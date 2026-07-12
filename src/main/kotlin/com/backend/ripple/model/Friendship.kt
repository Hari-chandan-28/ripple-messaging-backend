package com.backend.ripple.model
import jakarta.persistence.*

@Entity
@Table(name = "friendship")
class Friendship(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friendship_id")
    var friendshipId: Long =0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    var sender: User=User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    var receiver: User=User(),

    @Column(name = "status", nullable = false)
    var status: Int = 0
)