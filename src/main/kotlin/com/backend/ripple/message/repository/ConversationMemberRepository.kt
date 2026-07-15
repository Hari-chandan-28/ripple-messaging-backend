package com.backend.ripple.message.repository

import com.backend.ripple.model.ConversationMember
import com.backend.ripple.model.ConversationMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConversationMemberRepository : JpaRepository<ConversationMember, ConversationMemberId>
{
    fun existsById_ConversationIdAndId_UserId(conversationId: Long, userId: Long): Boolean
    fun findById_UserId(userId: Long): List<ConversationMember>
}