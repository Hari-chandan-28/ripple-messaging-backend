package com.backend.ripple.group.controller

import com.backend.ripple.dto.group.GroupMemberResponse
import com.backend.ripple.dto.group.GroupRequest
import com.backend.ripple.dto.group.GroupResponse
import com.backend.ripple.dto.group.GroupUpdateRequest
import com.backend.ripple.group.GroupRole
import com.backend.ripple.group.service.GroupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/groups")
class GroupController(private val groupService: GroupService) {
    @PostMapping("/create")
    fun createGroup(@RequestBody groupRequest: GroupRequest): ResponseEntity<GroupResponse>
    {
        val group = groupService.createGroup(groupRequest)
        return ResponseEntity.ok(group)
    }
    @PutMapping("/update")
    fun updateGroup(@RequestBody groupRequest: GroupUpdateRequest): ResponseEntity<GroupResponse>
    {
        val group = groupService.updateGroup(groupRequest)
        return ResponseEntity.ok(group)
    }
    @PostMapping("/{groupId}/add/{memberId}")
    fun addGroupMember(@PathVariable groupId: Long, @PathVariable memberId: Long): ResponseEntity<GroupMemberResponse>
    {
        val member = groupService.addGroupMember(memberId,groupId)
        return ResponseEntity.ok(member)
    }
    @DeleteMapping("/{groupId}/remove/{memberId}")
    fun removeGroupMember(@PathVariable groupId: Long, @PathVariable memberId: Long): ResponseEntity<Void>{
        groupService.removeMember(memberId,groupId)
        return ResponseEntity.noContent().build()
    }
    @DeleteMapping("delete/{groupId}")
    fun removeGroup(@PathVariable groupId: Long): ResponseEntity<Void>{
        groupService.deleteGroup(groupId)
        return ResponseEntity.noContent().build()
    }
    @GetMapping("/{groupId}")
    fun getGroupMember(@PathVariable groupId: Long): ResponseEntity<List<GroupMemberResponse>> {
        val groupMembers = groupService.getGroupMembers(groupId)
        return ResponseEntity.ok(groupMembers)
    }
    @DeleteMapping("/leave/{groupId}")
    fun removeGroupMember(@PathVariable groupId: Long): ResponseEntity<Void>{
        groupService.leaveGroup(groupId)
        return ResponseEntity.noContent().build()
    }
    @PutMapping("/{groupId}/role/{memberId}")
    fun changeRole(@PathVariable groupId: Long, @PathVariable memberId: Long, @RequestBody role: GroupRole): ResponseEntity<Void> {
        groupService.changeRole(memberId, groupId, role)
        return ResponseEntity.noContent().build()
    }
}