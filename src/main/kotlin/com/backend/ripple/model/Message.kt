package com.backend.ripple.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "message")
class Message(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    var messageId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    var conversation: Conversation = Conversation(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    var sender: User = User(),

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String = "",

    @Column(name = "sent_at", nullable = false)
    var sentAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false
)