package com.backend.ripple.model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "message_delete")
class MessageDelete(

    @EmbeddedId
    var id: MessageDeleteId = MessageDeleteId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    var user: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    var message: Message = Message()
)
@Embeddable
data class MessageDeleteId(
    var userId: Long = 0,
    var messageId: Long = 0
) : Serializable