package com.backend.ripple.group.service

import com.backend.ripple.AccessDeniedException
import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.dto.GroupMemberResponse
import com.backend.ripple.dto.GroupRequest
import com.backend.ripple.dto.GroupResponse
import com.backend.ripple.dto.GroupUpdateRequest
import com.backend.ripple.group.GroupRole
import com.backend.ripple.group.repository.GroupMemberRepository
import com.backend.ripple.group.repository.GroupRepository
import com.backend.ripple.model.Group
import com.backend.ripple.model.GroupMember
import com.backend.ripple.model.GroupMemberId
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Service


@Service
class GroupService (private val groupMemberRepository: GroupMemberRepository, private val groupRepository: GroupRepository,
    private val userRepository: UserRepository) {

    fun createGroup(group: GroupRequest): GroupResponse {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val user = userRepository.findById(userId).orElseThrow{ResourceNotFoundException("User not found")}
        val newGroup = Group(
            name = group.name,
            description = group.description,
            createdBy = user,
        )
        val ngroup =groupRepository.save(newGroup)
        val memberId = GroupMemberId(
            ngroup.groupId,
            ngroup.createdBy.userId
        )
        val groupMember = GroupMember(
            memberId,
            newGroup,
            user,
            role = GroupRole.OWNER,
        )
        groupMemberRepository.save(groupMember)
        return GroupResponse(
            ngroup.groupId,
            ngroup.name,
            ngroup.description,
            ngroup.createdBy.username
        )
    }
    fun updateGroup(updatedData: GroupUpdateRequest): GroupResponse {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        requireAdminOrAbove( updatedData.groupId, userId)
        val group = groupRepository.findByGroupId(updatedData.groupId).orElseThrow({ResourceNotFoundException("Group not found")})
        group.name = updatedData.name
        group.description = updatedData.description

        val result = groupRepository.save(group)
        return GroupResponse(
            result.groupId,
            result.name,
            result.description,
            result.createdBy.username
        )
    }
    fun addGroupMember(memberId:Long, groupId:Long): GroupMemberResponse {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        requireAdminOrAbove(groupId, userId)
        if(groupMemberRepository.findById_GroupIdAndId_MemberId(groupId, memberId).isPresent) {
            throw ResourceNotFoundException("Member already exists")
        }
        val group = groupRepository.findById(groupId).orElseThrow { ResourceNotFoundException("Group not found") }
        val member = userRepository.findById(memberId).orElseThrow { ResourceNotFoundException("User not found") }
        val newGroupMemberId = GroupMemberId(
            groupId,
            memberId,
        )
        val newGroupMember = GroupMember(
            newGroupMemberId,
            group,
            member,
            GroupRole.MEMBER
        )
        val result = groupMemberRepository.save(newGroupMember)
        return GroupMemberResponse(
            result.group.name,
            result.member.username,
            result.role
        )
    }
    fun removeMember(memberId:Long, groupId:Long){
        val result = roleBasedTask(memberId,groupId)
        val member= result[0]
        groupMemberRepository.delete(member)
    }
    fun changeRole(memberId:Long, groupId:Long,role:GroupRole){
        val result = roleBasedTask(memberId,groupId)
        val member= result[0]
        member.role = role
        groupMemberRepository.save(member)

    }
    private fun roleBasedTask(memberId:Long, groupId:Long):List<GroupMember>
    {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        requireAdminOrAbove(groupId, userId)
        val user= groupMemberRepository.findById_GroupIdAndId_MemberId(groupId, userId).orElseThrow { ResourceNotFoundException("User not found") }
        val member= groupMemberRepository.findById_GroupIdAndId_MemberId(groupId, memberId).orElseThrow { ResourceNotFoundException("User not found") }
        if(roleRank(user.role) <= roleRank(member.role)) {
            throw AccessDeniedException("You don't have permission to remove this member")
        }
        return listOf(member,user)
    }
    fun leaveGroup(groupId: Long) {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val member = groupMemberRepository.findById_GroupIdAndId_MemberId(groupId, userId)
            .orElseThrow { ResourceNotFoundException("You are not a member of this group") }
        if (member.role == GroupRole.OWNER) {
            throw AccessDeniedException("Owner cannot leave — delete the group or transfer ownership first")
        }
        groupMemberRepository.delete(member)
    }

    fun getGroupMembers(groupId: Long): List<GroupMemberResponse> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        groupMemberRepository.findById_GroupIdAndId_MemberId(groupId, userId)
            .orElseThrow { AccessDeniedException("You are not a member of this group") }
        return groupMemberRepository.findById_GroupId(groupId).map {
            GroupMemberResponse(it.group.name, it.member.username, it.role)
        }
    }

    fun deleteGroup(groupId: Long) {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val member = groupMemberRepository.findById_GroupIdAndId_MemberId(groupId, userId)
            .orElseThrow { AccessDeniedException("You are not a member of this group") }
        if (member.role != GroupRole.OWNER) {
            throw AccessDeniedException("Only the owner can delete the group")
        }
        groupRepository.deleteById(groupId)
    }

    private fun requireAdminOrAbove(groupId: Long, userId: Long) {
        val member = groupMemberRepository.findById_GroupIdAndId_MemberId(groupId, userId)
            .orElseThrow { AccessDeniedException("You are not a member of this group") }
        if (member.role == GroupRole.MEMBER) {
            throw AccessDeniedException("You don't have permission")
        }
    }
    fun roleRank(role: GroupRole): Int = when(role) {
        GroupRole.OWNER -> 3
        GroupRole.ADMIN -> 2
        else -> 1
    }



}