package com.backend.ripple.model.auth

import com.backend.ripple.model.profile.Profile
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "user")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var userId: Long = 0,

    @Column(name = "username", nullable = false, unique = true, length = 50)
    var username: String = "",

    @Column(name = "email", nullable = false, unique = true, length = 100)
    var email: String = "",

    @Column(name = "password", nullable = false, length = 255)
    var password: String? = "",

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,

    @Column(name = "last_seen")
    var lastSeen: LocalDateTime? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    var profile: Profile? = null
)