package com.backend.ripple.message.repository

import com.backend.ripple.model.message.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId AND m.isDeleted = false AND m.messageId NOT IN (SELECT md.id.messageId FROM MessageDelete md WHERE md.id.userId = :userId)")
    fun findMessagesForUser(@Param("conversationId") conversationId: Long, @Param("userId") userId: Long): List<Message>
    @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId AND m.isDeleted = false ORDER BY m.sentAt DESC LIMIT 1")
    fun findLastMessage(@Param("conversationId") conversationId: Long): Optional<Message>
}