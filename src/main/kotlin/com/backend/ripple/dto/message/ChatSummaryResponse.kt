package com.backend.ripple.dto.message

import com.backend.ripple.model.message.ConversationType

data class ChatSummaryResponse(
    val conversationId: Long,
    val type: ConversationType,
    val senderId:Long?=null,
    val receiverId:Long?=null,
    val name: String,
    val profilePic: String?,
    val lastMessage: String?,
    val lastMessageAt: String?
)