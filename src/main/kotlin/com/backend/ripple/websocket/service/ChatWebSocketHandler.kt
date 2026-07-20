package com.backend.ripple.websocket.service

import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.message.repository.ConversationMemberRepository
import com.backend.ripple.websocket.SessionStore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.message.repository.ConversationRepository
import com.backend.ripple.message.repository.MessageRepository
import com.backend.ripple.model.message.Message

@Service
class ChatWebSocketHandler(
    private val sessionStore: SessionStore,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
    private val conversationMemberRepository: ConversationMemberRepository,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository
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
                else -> session.sendMessage(TextMessage("""{"error": "unknown type: $type"}"""))
            }
        } catch (e: Exception) {
            session.sendMessage(TextMessage("""{"error": "invalid message format"}"""))
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
                "content" to content,
                "timestamp" to savedMessage.sentAt.toString()
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
                "deliveredAt" to savedMessage.sentAt.toString()
            )
        ))
        session.sendMessage(TextMessage(deliveredPacket))
    }
    private fun handleTyping(session: WebSocketSession, userId: Long, node: JsonNode) {
        val conversationId = node.get("payload")?.get("conversationId")?.asLong()
        if (conversationId == null) {
            session.sendMessage(TextMessage("""{"error": "missing conversationId or content"}"""))
            return
        }
        val members = conversationMemberRepository.findById_ConversationId(conversationId)
        val typingPacket = objectMapper.writeValueAsString(mapOf(
            "type" to "TYPING",
            "payload" to mapOf(
                "conversationId" to conversationId,
                "senderId" to userId,
                "isTyping" to (node.get("payload")?.get("isTyping")?.asBoolean() ?: false)
            )
        ))
        members.filter { it.id.userId != userId }.forEach { member ->
            sessionStore.sessions[member.id.userId]?.let { receiverSession ->
                if (receiverSession.isOpen) {
                    receiverSession.sendMessage(TextMessage(typingPacket))
                }
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
        val userId = session.attributes["userId"] as Long
        sessionStore.sessions.remove(userId)
        val user = userRepository.findById(userId).orElse(null) ?: return
        user.lastSeen = LocalDateTime.now()
        userRepository.save(user)
    }
}