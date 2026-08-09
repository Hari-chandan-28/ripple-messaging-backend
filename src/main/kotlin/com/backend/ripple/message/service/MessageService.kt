package com.backend.ripple.message.service

import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.dto.message.MessageResponse
import com.backend.ripple.message.repository.ConversationMemberRepository
import com.backend.ripple.message.repository.ConversationRepository
import com.backend.ripple.message.repository.MessageDeleteRepository
import com.backend.ripple.message.repository.MessageRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import com.backend.ripple.AccessDeniedException
import com.backend.ripple.UnauthorizedException
import com.backend.ripple.dto.message.ChatSummaryResponse
import com.backend.ripple.message.repository.MessageReadRepository
import com.backend.ripple.model.message.Conversation
import com.backend.ripple.model.message.ConversationMember
import com.backend.ripple.model.message.ConversationMemberId
import com.backend.ripple.model.message.ConversationType
import com.backend.ripple.model.message.MessageDelete
import com.backend.ripple.model.message.MessageDeleteId
import com.backend.ripple.profile.repository.ProfileRepository
import org.springframework.transaction.annotation.Transactional

@Service
class MessageService(
    private val messageDeleteRepository: MessageDeleteRepository,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val conversationMemberRepository: ConversationMemberRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val messageReadRepository: MessageReadRepository,
){
    @Transactional(readOnly = true)
    fun getChats(): List<ChatSummaryResponse> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val memberships = conversationMemberRepository.findById_UserId(userId)

        return memberships.map { membership ->
            val conversation = membership.conversation
            val allMessages = messageRepository.findMessagesForUser(conversation.conversationId, userId)
            val lastMessage = allMessages.lastOrNull()

            // Count messages not sent by me and not in message_read table
            val unreadCount = allMessages.count { msg ->
                msg.sender.userId != userId &&
                        !msg.isDeleted &&
                        !messageReadRepository.existsById_MessageIdAndId_UserId(msg.messageId, userId)
            }

            if (conversation.type == ConversationType.GROUP) {
                ChatSummaryResponse(
                    conversationId = conversation.conversationId,
                    type = ConversationType.GROUP,
                    groupId = conversation.group?.groupId,
                    name = conversation.group?.name ?: "Group",
                    profilePic = conversation.group?.profilePic,
                    description = conversation.group?.description,
                    lastMessage = if (lastMessage?.isDeleted == true) "This message was deleted" else lastMessage?.content,
                    lastMessageAt = lastMessage?.sentAt?.toString(),
                    unreadCount = unreadCount,
                    lastSenderId = lastMessage?.sender?.userId,
                )
            } else {
                val otherMember = conversationMemberRepository
                    .findById_ConversationId(conversation.conversationId)
                    .firstOrNull { it.id.userId != userId }
                val otherUser = otherMember?.user
                val profile = otherUser?.profile
                ChatSummaryResponse(
                    conversationId = conversation.conversationId,
                    type = ConversationType.PRIVATE,
                    groupId = null,
                    name = profile?.name ?: otherUser?.username ?: "Unknown",
                    profilePic = profile?.profilePic,
                    description = null,
                    lastMessage = if (lastMessage?.isDeleted == true) "This message was deleted" else lastMessage?.content,
                    lastMessageAt = lastMessage?.sentAt?.toString(),
                    unreadCount = unreadCount,
                    lastSenderId = lastMessage?.sender?.userId,
                )
            }
        }.sortedByDescending { it.lastMessageAt }
    }
    @Transactional
    fun getMessages(conversationId: Long): List<MessageResponse> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val members = conversationMemberRepository.findById_ConversationId(conversationId)
        val totalMembers = members.size

        return messageRepository.findMessagesForUser(conversationId, userId).map { message ->
            val readCount = messageReadRepository.countByMessage_MessageId(message.messageId).toInt()
            val allRead = readCount >= totalMembers - 1

            MessageResponse(
                convId = conversationId,
                messageId = message.messageId,
                senderId = message.sender.userId,
                senderUsername = message.sender.username,
                content = message.content,
                sendAt = message.sentAt.toString(),
                isDeleted = message.isDeleted,
                isRead = allRead,
            )
        }
    }
    fun editMessage(messageId: Long, newContent: String){
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val message = messageRepository.findById(messageId).orElseThrow { ResourceNotFoundException("Message not found") }
        val conversationId = message.conversation.conversationId
        if(message.sender.userId != userId){
            throw AccessDeniedException("You can't change the message")
        }
        message.content = newContent
        messageRepository.save(message)
    }
    fun deleteMessage(messageId: Long, deleteType: String) {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val message = messageRepository.findById(messageId)
            .orElseThrow { ResourceNotFoundException("Message not found") }

        if (deleteType == "deleteForEveryone") {
            if (message.sender.userId != userId)
                throw UnauthorizedException("Not your message")
            message.isDeleted = true
            messageRepository.save(message)
        } else {
            // deleteForMe
            val user = userRepository.findById(userId)
                .orElseThrow { ResourceNotFoundException("User not found") }
            val deleteId = MessageDeleteId(userId, messageId)
            if (!messageDeleteRepository.existsById(deleteId)) {
                messageDeleteRepository.save(
                    MessageDelete(id = deleteId, user = user, message = message)
                )
            }
        }
    }
    fun createConversation(receiverId: Long): Long {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val existing = conversationRepository.findDirectConversation(userId, receiverId)
        if (existing.isPresent) return existing.get().conversationId

        val sender = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User not found") }
        val receiver = userRepository.findById(receiverId).orElseThrow { ResourceNotFoundException("User not found") }

        val conversation = Conversation(type = ConversationType.PRIVATE)
        val savedConversation = conversationRepository.save(conversation)

        val senderMember = ConversationMember(
            id = ConversationMemberId(savedConversation.conversationId, userId),
            conversation = savedConversation,
            user = sender
        )
        val receiverMember = ConversationMember(
            id = ConversationMemberId(savedConversation.conversationId, receiverId),
            conversation = savedConversation,
            user = receiver
        )
        conversationMemberRepository.save(senderMember)
        conversationMemberRepository.save(receiverMember)

        return savedConversation.conversationId
    }

}