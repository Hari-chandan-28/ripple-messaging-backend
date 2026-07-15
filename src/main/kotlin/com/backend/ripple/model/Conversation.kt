package com.backend.ripple.model

import jakarta.persistence.*

@Entity
@Table(name = "conversation")
class Conversation(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    var conversationId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: ConversationType = ConversationType.PRIVATE,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group? = null
)
enum class ConversationType {
    PRIVATE,
    GROUP
}