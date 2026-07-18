package com.backend.ripple.message.repository

import com.backend.ripple.model.message.ConversationMember
import com.backend.ripple.model.message.ConversationMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ConversationMemberRepository : JpaRepository<ConversationMember, ConversationMemberId>
{
    fun existsById_ConversationIdAndId_UserId(conversationId: Long, userId: Long): Boolean
    fun findById_ConversationIdAndId_UserId(conversationId: Long, userId: Long): Optional<ConversationMember>
    fun findById_UserId(userId: Long): List<ConversationMember>
    fun findById_ConversationId(conversationId: Long): List<ConversationMember>
}