package com.backend.ripple.model

import com.backend.ripple.group.GroupRole
import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "group_member")
class GroupMember(

    @EmbeddedId
    var id: GroupMemberId = GroupMemberId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    var group: Group = Group(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    var member: User = User(),

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: GroupRole = GroupRole.MEMBER
)

@Embeddable
data class GroupMemberId(
    var groupId: Long = 0,
    var memberId: Long = 0
) : Serializable