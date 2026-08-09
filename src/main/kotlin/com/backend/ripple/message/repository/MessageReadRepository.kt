package com.backend.ripple.message.repository



import com.backend.ripple.model.message.MessageRead
import com.backend.ripple.model.message.MessageReadId
import org.springframework.data.jpa.repository.JpaRepository

interface MessageReadRepository : JpaRepository<MessageRead, MessageReadId> {
    fun countByMessage_MessageId(messageId: Long): Long
    fun existsById_MessageIdAndId_UserId(messageId: Long, userId: Long): Boolean
}