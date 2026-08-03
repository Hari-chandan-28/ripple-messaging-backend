package com.backend.ripple.group.service

import com.backend.ripple.AccessDeniedException
import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.dto.group.GroupCreateWithMembersRequest
import com.backend.ripple.dto.group.GroupMemberResponse
import com.backend.ripple.dto.group.GroupRequest
import com.backend.ripple.dto.group.GroupResponse
import com.backend.ripple.dto.group.GroupUpdateRequest
import com.backend.ripple.group.GroupRole
import com.backend.ripple.group.repository.GroupMemberRepository
import com.backend.ripple.group.repository.GroupRepository
import com.backend.ripple.message.repository.ConversationMemberRepository
import com.backend.ripple.message.repository.ConversationRepository
import com.backend.ripple.model.message.Conversation
import com.backend.ripple.model.message.ConversationMember
import com.backend.ripple.model.message.ConversationMemberId
import com.backend.ripple.model.message.ConversationType
import com.backend.ripple.model.group.Group
import com.backend.ripple.model.group.GroupMember
import com.backend.ripple.model.group.GroupMemberId
import com.backend.ripple.websocket.service.NotificationService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
class GroupService (private val groupMemberRepository: GroupMemberRepository, private val groupRepository: GroupRepository,
                    private val userRepository: UserRepository, private val conversationRepository: ConversationRepository,
                    private val conversationMemberRepository: ConversationMemberRepository, private val notificationService: NotificationService) {

    fun createGroup(group: GroupRequest): GroupResponse {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val user = userRepository.findById(userId).orElseThrow{ResourceNotFoundException("User not found")}
        val newGroup = Group(
            name = group.name,
            description = group.description,
            createdBy = user,
        )
        val nGroup =groupRepository.save(newGroup)
        val memberId = GroupMemberId(
            nGroup.groupId,
            nGroup.createdBy.userId
        )
        val groupMember = GroupMember(
            memberId,
            newGroup,
            user,
            role = GroupRole.OWNER,
        )
        groupMemberRepository.save(groupMember)
        val conversation = Conversation(
            type = ConversationType.GROUP,
            group = nGroup
        )
        val savedConversation = conversationRepository.save(conversation)
        val conversationMember = ConversationMember(
            id = ConversationMemberId(savedConversation.conversationId, userId),
            conversation = savedConversation,
            user = user
        )
        conversationMemberRepository.save(conversationMember)
        return GroupResponse(
            nGroup.groupId,
            nGroup.name,
            nGroup.description,
            nGroup.createdBy.username
        )
    }
    fun createGroupWithMembers(request: GroupCreateWithMembersRequest): GroupResponse {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User not found") }

        val newGroup = Group(
            name = request.name,
            description = request.description,
            profilePic = request.profilePic,
            createdBy = user,
            createdAt = LocalDateTime.now()
        )
        val savedGroup = groupRepository.save(newGroup)

        // Add creator as OWNER
        val ownerId = GroupMemberId(savedGroup.groupId, userId)
        val ownerMember = GroupMember(ownerId, savedGroup, user, role = GroupRole.OWNER)
        groupMemberRepository.save(ownerMember)

        // Create conversation for this group
        val conversation = Conversation(type = ConversationType.GROUP, group = savedGroup)
        val savedConversation = conversationRepository.save(conversation)
        val ownerConvMember = ConversationMember(
            id = ConversationMemberId(savedConversation.conversationId, userId),
            conversation = savedConversation, user = user
        )
        conversationMemberRepository.save(ownerConvMember)

        // Add all requested members
        request.memberIds.filter { it != userId }.forEach { memberId ->
            val member = userRepository.findById(memberId).orElse(null) ?: return@forEach
            val gmId = GroupMemberId(savedGroup.groupId, memberId)
            val gm = GroupMember(gmId, savedGroup, member, role = GroupRole.MEMBER)
            groupMemberRepository.save(gm)
            val cmId = ConversationMemberId(savedConversation.conversationId, memberId)
            val cm = ConversationMember(id = cmId, conversation = savedConversation, user = member)
            conversationMemberRepository.save(cm)
            // Notify each added member via WebSocket
            notificationService.notifyAddedToGroup(memberId, savedGroup.groupId, savedGroup.name, userId, user.username)
        }

        return GroupResponse(savedGroup.groupId, savedGroup.name, savedGroup.description, user.username)
    }
    @Transactional
    fun updateGroup(updatedData: GroupUpdateRequest): GroupResponse {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        requireAdminOrAbove( updatedData.groupId, userId)
        val group = groupRepository.findByGroupId(updatedData.groupId).orElseThrow({ResourceNotFoundException("Group not found")})
        group.name = updatedData.name ?: group.name
        group.description = updatedData.description
        group.profilePic = updatedData.profilePic ?: group.profilePic
        val result = groupRepository.save(group)
        return GroupResponse(
            result.groupId,
            result.name,
            result.description,
            result.createdBy.username
        )
    }
//fun updateGroup(request: GroupUpdateRequest): GroupResponse {
//    val userId = SecurityContextHolder.getContext().authentication?.principal as Long
//    requireAdminOrAbove( request.groupId, userId)
//    val group = groupRepository.findById(request.groupId)
//        .orElseThrow { ResourceNotFoundException("Group not found") }
//    group.name = request.name ?: group.name
//    group.description = request.description ?: group.description
//    group.profilePic = request.profilePic ?: group.profilePic
//    groupRepository.save(group)
//    return GroupResponse(group.groupId, group.name, group.description, group.createdBy.username)
//}
//    fun addGroupMember(memberId:Long, groupId:Long): GroupMemberResponse {
//        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
//        requireAdminOrAbove(groupId, userId)
//        if(groupMemberRepository.findById_GroupIdAndId_MemberId(groupId, memberId).isPresent) {
//            throw ResourceNotFoundException("Member already exists")
//        }
//        val group = groupRepository.findById(groupId).orElseThrow { ResourceNotFoundException("Group not found") }
//        val member = userRepository.findById(memberId).orElseThrow { ResourceNotFoundException("User not found") }
//        val newGroupMemberId = GroupMemberId(
//            groupId,
//            memberId,
//        )
//        val newGroupMember = GroupMember(
//            newGroupMemberId,
//            group,
//            member,
//            GroupRole.MEMBER
//        )
//        val result = groupMemberRepository.save(newGroupMember)
//        val conversation = conversationRepository.findByGroup_GroupId(groupId).orElseThrow({
//            ResourceNotFoundException("Group not found")
//        })
//        val conversationMember = ConversationMember(
//            id = ConversationMemberId(conversation.conversationId, memberId),
//            conversation = conversation,
//            user = member
//        )
//        conversationMemberRepository.save(conversationMember)
//        return GroupMemberResponse(
//            memberId = result.member.userId,
//            memberUsername = result.member.username,
//            memberName = result.member.profile?.name,
//            memberProfilePic = result.member.profile?.profilePic,
//            role = result.role.name
//        )
//    }
@Transactional
fun addGroupMember(groupId: Long, memberId: Long): GroupMemberResponse {
    val userId = SecurityContextHolder.getContext().authentication?.principal as Long
    if(groupMemberRepository.findById_GroupIdAndId_MemberId(groupId, memberId).isPresent) {
            throw ResourceNotFoundException("Member already exists")
        }
        requireAdminOrAbove(groupId, userId)

    val group = groupRepository.findById(groupId)
        .orElseThrow { ResourceNotFoundException("Group not found") }

    // Eager fetch user with profile
    val member = userRepository.findById(memberId)
        .orElseThrow { ResourceNotFoundException("User not found") }

    // Add to group
    val gmId = GroupMemberId(groupId, memberId)
    if (!groupMemberRepository.existsById(gmId)) {
        groupMemberRepository.save(GroupMember(gmId, group, member, role = GroupRole.MEMBER))
    }

    // Add to conversation
    val conversation = conversationRepository
        .findByGroup_GroupId(groupId)
        .orElseThrow { ResourceNotFoundException("Conversation not found") }
    val cmId = ConversationMemberId(conversation.conversationId, memberId)
    if (!conversationMemberRepository.existsById(cmId)) {
        conversationMemberRepository.save(
            ConversationMember(id = cmId, conversation = conversation, user = member)
        )
    }

    // Notify — extract values BEFORE returning so proxy is still in session
    val groupName = group.name
    val adderUsername = userRepository.findById(userId)
        .orElseThrow { ResourceNotFoundException("User not found") }.username

    notificationService.notifyAddedToGroup(memberId, groupId, groupName, userId, adderUsername)

    return GroupMemberResponse(
        memberId = member.userId,
        memberUsername = member.username,
        memberName = member.profile?.name,
        memberProfilePic = member.profile?.profilePic,
        role = GroupRole.MEMBER.name,
    )
}
    fun removeMember(memberId:Long, groupId:Long){
        val result = roleBasedTask(memberId,groupId)
        val member= result[0]
        val conversation = conversationRepository.findByGroup_GroupId(groupId).orElseThrow({
            ResourceNotFoundException("Group not found")
        })
        val conversationMember = conversationMemberRepository.findById_ConversationIdAndId_UserId(conversation.conversationId, memberId).orElseThrow { ResourceNotFoundException("User Conversation not found") }
        conversationMemberRepository.delete(conversationMember)
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
        val conversation = conversationRepository.findByGroup_GroupId(groupId).orElseThrow({
            ResourceNotFoundException("Group not found")
        })
        val conversationMember = conversationMemberRepository.findById_ConversationIdAndId_UserId(conversation.conversationId, userId).orElseThrow { ResourceNotFoundException("User Conversation not found") }
        conversationMemberRepository.delete(conversationMember)
        groupMemberRepository.delete(member)
    }

    @Transactional(readOnly = true)
    fun getGroupMembers(groupId: Long): List<GroupMemberResponse> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        groupMemberRepository.findById_GroupIdAndId_MemberId(groupId, userId)
            .orElseThrow { AccessDeniedException("You are not a member of this group") }
        return groupMemberRepository.findById_GroupId(groupId).map {
            GroupMemberResponse(
                memberId = it.member.userId,
                memberUsername = it.member.username,
                memberName = it.member.profile?.name,
                memberProfilePic = it.member.profile?.profilePic,
                role = it.role.name,
            )
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