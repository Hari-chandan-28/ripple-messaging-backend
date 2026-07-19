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
import com.backend.ripple.dto.message.ChatSummaryResponse
import com.backend.ripple.model.message.Conversation
import com.backend.ripple.model.message.ConversationMember
import com.backend.ripple.model.message.ConversationMemberId
import com.backend.ripple.model.message.ConversationType
import com.backend.ripple.model.message.MessageDelete
import com.backend.ripple.model.message.MessageDeleteId
import com.backend.ripple.profile.repository.ProfileRepository

@Service
class MessageService(
    private val messageDeleteRepository: MessageDeleteRepository,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val conversationMemberRepository: ConversationMemberRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository
){
    fun getChats(): List<ChatSummaryResponse> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User not found") }

        return conversationMemberRepository.findById_UserId(userId).map { member ->
            val conversation = member.conversation
            val lastMessage = messageRepository.findLastMessage(conversation.conversationId).orElse(null)
            if (conversation.type == ConversationType.GROUP) {
                // group chat — use group name
                val groupName = conversation.group?.name ?: "Group"
                ChatSummaryResponse(
                    conversationId = conversation.conversationId,
                    type = conversation.type,
                    name = groupName,
                    profilePic = null,
                    lastMessage = lastMessage?.content,
                    lastMessageAt = lastMessage?.sentAt?.toString()
                )
            } else {
                // direct chat — find the other person
                val otherMember = conversationMemberRepository
                    .findById_ConversationId(conversation.conversationId)
                    .firstOrNull { it.id.userId != userId }

                val otherUser = otherMember?.let {
                    userRepository.findById(it.id.userId).orElse(null)
                }
                val otherProfile = otherUser?.let {
                    profileRepository.findByUserId(it.userId).orElse(null)
                }

                ChatSummaryResponse(
                    conversationId = conversation.conversationId,
                    type = conversation.type,
                    name = otherProfile?.name ?: otherUser?.username ?: "Unknown",
                    profilePic = otherProfile?.profilePic,
                    lastMessage = lastMessage?.content,
                    lastMessageAt = lastMessage?.sentAt?.toString()
                )
            }
        }
    }
    fun getMessages(conversationId: Long): List<MessageResponse>{
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val messages = messageRepository.findMessagesForUser(conversationId, userId)
        val list = mutableListOf<MessageResponse>()
        for (message in messages){
            val chat= MessageResponse(
             convId=conversationId,
             messageId = message.messageId ,
             senderId = message.sender.userId ,
             content = message.content ,
             sendAt = message.sentAt)
            list.add(chat)
        }
    return list
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
    fun deleteMessage(messageId: Long, deleteType: String){
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User not found") }
        val message = messageRepository.findById(messageId).orElseThrow { ResourceNotFoundException("Message not found") }
        if (message.isDeleted) {
            return
        }
        if(message.sender.userId != userId){
            if(deleteType == "deleteForEveryone"){
                throw AccessDeniedException("You can't delete the message")
            }
            val delete = MessageDeleteId(userId, messageId)
            val deleteMessage = MessageDelete(
                delete,
                user,
                message
            )
            messageDeleteRepository.save(deleteMessage)
        }
        else
        {
            if(deleteType == "deleteForEveryone"){
                message.isDeleted = true
                messageRepository.save(message)
            }
            else{
                val deleteId = MessageDeleteId(userId, messageId)
                val deleteRecord = MessageDelete(deleteId, user, message)
                messageDeleteRepository.save(deleteRecord)
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