package com.backend.ripple.websocket.service

import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.message.repository.ConversationMemberRepository
import com.backend.ripple.websocket.SessionStore
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.UnauthorizedException
import com.backend.ripple.friendship.repository.FriendshipRepository
import com.backend.ripple.message.repository.ConversationRepository
import com.backend.ripple.message.repository.MessageDeleteRepository
import com.backend.ripple.message.repository.MessageRepository
import com.backend.ripple.model.message.ConversationType
import com.backend.ripple.model.message.Message
import com.backend.ripple.model.message.MessageDelete
import com.backend.ripple.model.message.MessageDeleteId

@Service
class ChatWebSocketHandler(
    private val sessionStore: SessionStore,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
    private val conversationMemberRepository: ConversationMemberRepository,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val friendshipRepository: FriendshipRepository,
    private val messageDeleteRepository: MessageDeleteRepository
    ) : TextWebSocketHandler(){
    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as Long
        val existingSession= SessionStore.sessions[userId]
        if(SessionStore.sessions.containsKey(userId) && existingSession?.isOpen == true)
        {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Session already open"))
        }
        SessionStore.sessions[userId] = session
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val userId = session.attributes["userId"] as Long
        try {
            val node = objectMapper.readTree(message.payload)
            val type = node.get("type")?.asString()
            if (type == null) {
                session.sendMessage(TextMessage("""{"error": "missing type field"}"""))
                return
            }
            when (type) {
                "SEND_MESSAGE" -> handleSendMessage(session, userId, node)
                "TYPING" -> handleTyping(session, userId, node)
                "READ_RECEIPT" -> handleReadReceipt(session, userId, node)
                "EDIT_MESSAGE" -> handleEditMessage(session, userId, node)
                "DELETE_MESSAGE" -> handleDeleteMessage(session, userId, node)
                else -> session.sendMessage(TextMessage("""{"error": "unknown type: $type"}"""))
            }
        } catch (e: Exception) {
            session.sendMessage(TextMessage("""{"error": "invalid message format"}"""))
        }
    }
    private fun handleDeleteMessage(session: WebSocketSession, userId: Long, node: JsonNode) {
        val messageId = node.get("payload")?.get("messageId")?.asLong() ?: run {
            session.sendMessage(TextMessage("""{"error": "messageId required"}"""))
            return
        }
        val deleteType = node.get("payload")?.get("deleteType")?.asText() ?: "deleteForMe"
        val conversationId = node.get("payload")?.get("conversationId")?.asLong() ?: run {
            session.sendMessage(TextMessage("""{"error": "conversationId required"}"""))
            return
        }

        val message = messageRepository.findById(messageId).orElse(null) ?: run {
            session.sendMessage(TextMessage("""{"error": "message not found"}"""))
            return
        }

        if (message.sender.userId != userId) {
            session.sendMessage(TextMessage("""{"error": "not your message"}"""))
            return
        }

        if (deleteType == "deleteForEveryone") {
            message.isDeleted = true
            messageRepository.save(message)

            // Broadcast to all members
            val packet = objectMapper.writeValueAsString(mapOf(
                "type" to "DELETE_MESSAGE",
                "payload" to mapOf(
                    "messageId" to messageId,
                    "conversationId" to conversationId,
                    "deleteType" to "deleteForEveryone",
                )
            ))
            val members = conversationMemberRepository.findById_ConversationId(conversationId)
            members.forEach { member ->
                SessionStore.sessions[member.id.userId]?.let { s ->
                    if (s.isOpen) s.sendMessage(TextMessage(packet))
                }
            }
        } else {
            // deleteForMe — save to message_delete table
            val user = userRepository.findById(userId).orElse(null) ?: return
            val deleteId = MessageDeleteId(userId, messageId)
            if (!messageDeleteRepository.existsById(deleteId)) {
                messageDeleteRepository.save(MessageDelete(id = deleteId, user = user, message = message))
            }
            // Only notify sender — no broadcast needed
            val packet = objectMapper.writeValueAsString(mapOf(
                "type" to "DELETE_MESSAGE",
                "payload" to mapOf(
                    "messageId" to messageId,
                    "conversationId" to conversationId,
                    "deleteType" to "deleteForMe",
                )
            ))
            session.sendMessage(TextMessage(packet))
        }
    }
    private fun handleEditMessage(session: WebSocketSession, userId: Long, node: JsonNode) {
        val messageId = node.get("payload")?.get("messageId")?.asLong() ?: run {
            session.sendMessage(TextMessage("""{"error": "messageId required"}"""))
            return
        }
        val content = node.get("payload")?.get("content")?.asText()?.trim() ?: run {
            session.sendMessage(TextMessage("""{"error": "content required"}"""))
            return
        }
        if (content.isEmpty()) {
            session.sendMessage(TextMessage("""{"error": "content cannot be empty"}"""))
            return
        }

        val message = messageRepository.findById(messageId).orElse(null) ?: run {
            session.sendMessage(TextMessage("""{"error": "message not found"}"""))
            return
        }

        if (message.sender.userId != userId) {
            session.sendMessage(TextMessage("""{"error": "not your message"}"""))
            return
        }

        if (message.isDeleted) {
            session.sendMessage(TextMessage("""{"error": "cannot edit deleted message"}"""))
            return
        }

        message.content = content
        val saved = messageRepository.save(message)

        // Use conversationId from the saved message — don't trust client
        val conversationId = saved.conversation.conversationId
        val sender = userRepository.findById(userId).orElse(null) ?: return

        val packet = objectMapper.writeValueAsString(mapOf(
            "type" to "EDIT_MESSAGE",
            "payload" to mapOf(
                "messageId" to saved.messageId,
                "conversationId" to conversationId,
                "senderId" to userId,
                "senderUsername" to sender.username,
                "content" to saved.content,
                "timestamp" to saved.sentAt.toString(),
                "isDeleted" to false,
            )
        ))

        val members = conversationMemberRepository.findById_ConversationId(conversationId)
        members.forEach { member ->
            SessionStore.sessions[member.id.userId]?.let { s ->
                if (s.isOpen) s.sendMessage(TextMessage(packet))
            }
        }
    }
    private fun handleSendMessage(session: WebSocketSession, userId: Long, node: JsonNode) {
        val conversationId = node.get("payload")?.get("conversationId")?.asLong()
        val content = node.get("payload")?.get("content")?.asString()

        if (conversationId == null || content.isNullOrBlank()) {
            session.sendMessage(TextMessage("""{"error": "missing conversationId or content"}"""))
            return
        }
        if (!conversationMemberRepository.existsById_ConversationIdAndId_UserId(conversationId, userId)) {
            session.sendMessage(TextMessage("""{"error": "not a member of this conversation"}"""))
            return
        }
        val sender = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User not found") }
        val conversation = conversationRepository.findById(conversationId).orElseThrow { ResourceNotFoundException("Conversation not found") }
        if (conversation.type == ConversationType.PRIVATE) {
            val members = conversationMemberRepository.findById_ConversationId(conversationId)
            val otherUserId = members.firstOrNull { it.id.userId != userId }?.id?.userId
            if (otherUserId != null) {
                val areFriends = friendshipRepository.existsBySender_UserIdAndReceiver_UserId(userId, otherUserId) ||
                        friendshipRepository.existsBySender_UserIdAndReceiver_UserId(otherUserId, userId)
                val friendship = if (areFriends) {
                    friendshipRepository.findBySender_UserIdAndReceiver_UserId(userId, otherUserId)
                        .orElse(friendshipRepository.findBySender_UserIdAndReceiver_UserId(otherUserId, userId).orElse(null))
                } else null

                if (friendship == null || friendship.status != 2) {
                    session.sendMessage(TextMessage("""{"error": "not_friends", "message": "You are no longer friends"}"""))
                    return
                }
            }
        }
        val message = Message(
            conversation = conversation,
            sender = sender,
            content = content,
            sentAt = LocalDateTime.now()
        )
        val savedMessage = messageRepository.save(message)
        val members = conversationMemberRepository.findById_ConversationId(conversationId)
        val receivePacket = objectMapper.writeValueAsString(mapOf(
            "type" to "RECEIVE_MESSAGE",
            "payload" to mapOf(
                "messageId" to savedMessage.messageId,
                "conversationId" to conversationId,
                "senderId" to userId,
                "senderUsername" to sender.username,  // add this
                "content" to savedMessage.content,
                "timestamp" to savedMessage.sentAt.toString(),
                "isDeleted" to false,
            )
        ))
        members.filter { it.id.userId != userId }.forEach { member ->
            sessionStore.sessions[member.id.userId]?.let { receiverSession ->
                if (receiverSession.isOpen) {
                    receiverSession.sendMessage(TextMessage(receivePacket))
                }
            }
        }
        val deliveredPacket = objectMapper.writeValueAsString(mapOf(
            "type" to "MESSAGE_DELIVERED",
            "payload" to mapOf(
                "messageId" to savedMessage.messageId,
                "conversationId" to conversationId,
                "deliveredAt" to savedMessage.sentAt.toString(),
                "content" to savedMessage.content,  // add this
            )
        ))
        session.sendMessage(TextMessage(deliveredPacket))
    }
    private fun handleTyping(session: WebSocketSession, userId: Long, node: JsonNode) {
        val conversationId = node.get("payload")?.get("conversationId")?.asLong() ?: return
        val isTyping = node.get("payload")?.get("isTyping")?.asBoolean() ?: return

        val members = conversationMemberRepository.findById_ConversationId(conversationId)
        val typingPacket = objectMapper.writeValueAsString(mapOf(
            "type" to "TYPING",
            "payload" to mapOf(
                "conversationId" to conversationId,
                "senderId" to userId,
                "isTyping" to isTyping,
            )
        ))
        members.filter { it.id.userId != userId }.forEach { member ->
            SessionStore.sessions[member.id.userId]?.let { s ->
                if (s.isOpen) s.sendMessage(TextMessage(typingPacket))
            }
        }
    }
    private fun handleReadReceipt(session: WebSocketSession, userId: Long, node: JsonNode) {
        val messageId = node.get("payload")?.get("messageId")?.asLong()
        val conversationId = node.get("payload")?.get("conversationId")?.asLong()
        // TODO: persist read receipts to DB for offline delivery
        // TODO: Currently only works in real time — sender must be online when receiver reads
        if (messageId == null || conversationId == null) {
            session.sendMessage(TextMessage("""{"error": "missing messageId or conversationId"}"""))
            return
        }
        val message = messageRepository.findById(messageId).orElse(null) ?: return
        val senderId = message.sender.userId

        val readPacket = objectMapper.writeValueAsString(mapOf(
            "type" to "READ_RECEIPT",
            "payload" to mapOf(
                "messageId" to messageId,
                "readBy" to userId,
                "readAt" to LocalDateTime.now().toString()
            )
        ))
        sessionStore.sessions[senderId]?.let { senderSession ->
            if (senderSession.isOpen) {
                senderSession.sendMessage(TextMessage(readPacket))
            }
        }
    }
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = session.attributes["userId"] as? Long ?: return
        SessionStore.sessions.remove(userId)
        try {
            val user = userRepository.findById(userId).orElse(null) ?: return
            user.lastSeen = LocalDateTime.now()
            userRepository.save(user)
        } catch (e: Exception) {
            println("Failed to update lastSeen for user $userId: ${e.message}")
        }
    }
}