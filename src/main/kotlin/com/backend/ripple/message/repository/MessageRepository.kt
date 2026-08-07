package com.backend.ripple.message.repository

import com.backend.ripple.model.message.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    @Query("""
    SELECT m FROM Message m 
    WHERE m.conversation.conversationId = :conversationId
    AND NOT EXISTS (
        SELECT md FROM MessageDelete md 
        WHERE md.id.messageId = m.messageId 
        AND md.id.userId = :userId
    )
    ORDER BY m.sentAt ASC
""")
    fun findMessagesForUser(
        @Param("conversationId") conversationId: Long,
        @Param("userId") userId: Long
    ): List<Message>
    @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId AND m.isDeleted = false ORDER BY m.sentAt DESC LIMIT 1")
    fun findLastMessage(@Param("conversationId") conversationId: Long): Optional<Message>
}