package com.backend.ripple.model
import com.backend.ripple.dto.RelationshipStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.Table

@Entity
@Table(name = "profile")
class Profile(

    @Id
    @Column(name = "user_id")
    var userId: Long = 0,

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(name = "name", nullable = false)
    var name: String="",

    @Column(name = "bio")
    var bio: String? = null,

    @Column(name = "profile_pic")
    var profilePic: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_status")
    var relationshipStatus: RelationshipStatus? = null

)
