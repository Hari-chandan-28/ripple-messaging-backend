package com.backend.ripple.model.message


import com.backend.ripple.model.auth.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "message_read")
data class MessageRead(
    @EmbeddedId
    val id: MessageReadId,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    val message: Message,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    val user: User,

    val readAt: LocalDateTime = LocalDateTime.now()
)