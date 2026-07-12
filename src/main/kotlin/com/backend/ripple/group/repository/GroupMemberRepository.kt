package com.backend.ripple.group.repository

import com.backend.ripple.model.Group
import com.backend.ripple.model.GroupMember
import com.backend.ripple.model.GroupMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional


@Repository
interface GroupMemberRepository : JpaRepository<GroupMember, GroupMemberId> {
    fun existsById_GroupIdAndId_MemberId(groupId: Long, memberId: Long): Boolean
    fun findById_GroupIdAndId_MemberId(groupId: Long, memberId: Long): Optional<GroupMember>
    fun findById_GroupId(groupId: Long): List<GroupMember>
}