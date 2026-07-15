package com.backend.ripple.model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "conversation_member")
class ConversationMember(

    @EmbeddedId
    var id: ConversationMemberId = ConversationMemberId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    var conversation: Conversation = Conversation(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    var user: User = User()
)

@Embeddable
data class ConversationMemberId(
    var conversationId: Long = 0,
    var userId: Long = 0
) : Serializable