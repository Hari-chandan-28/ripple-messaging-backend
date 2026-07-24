package com.backend.ripple.dto.message

import java.time.LocalDateTime

data class MessageResponse(
    val messageId: Long,
    val convId: Long,
    val senderId: Long,
    val content: String,
    val sendAt: String,
    val isDeleted: Boolean,
)