package com.backend.ripple.model.message


import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class MessageReadId(
    val messageId: Long = 0,
    val userId: Long = 0,
) : Serializable
