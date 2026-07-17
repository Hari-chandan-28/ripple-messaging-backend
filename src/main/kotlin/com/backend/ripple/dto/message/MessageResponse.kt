package com.backend.ripple.dto.message

import com.backend.ripple.model.Conversation
import com.backend.ripple.model.User
import java.time.LocalDateTime

data class MessageResponse (
    val convId: Long,
    val messageId: Long,
    val senderId: Long,
    val content: String,
    val sendAt: LocalDateTime,
    )
