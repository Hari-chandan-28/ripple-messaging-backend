package com.backend.ripple.message.repository

import com.backend.ripple.model.Conversation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ConversationRepository : JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c JOIN ConversationMember cm1 ON cm1.id.conversationId = c.conversationId JOIN ConversationMember cm2 ON cm2.id.conversationId = c.conversationId WHERE cm1.id.userId = :userId1 AND cm2.id.userId = :userId2 AND c.type = 'PRIVATE'")
    fun findDirectConversation(@Param("userId1") userId1: Long, @Param("userId2") userId2: Long): Optional<Conversation>
}