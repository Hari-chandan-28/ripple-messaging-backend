package com.backend.ripple.dto.message

import com.backend.ripple.model.message.ConversationType

data class ChatSummaryResponse(
    val conversationId: Long,
    val type: ConversationType,
    val name: String,
    val profilePic: String?,
    val lastMessage: String?,
    val lastMessageAt: String?
)