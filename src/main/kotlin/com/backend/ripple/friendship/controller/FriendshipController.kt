package com.backend.ripple.friendship.controller

import com.backend.ripple.dto.friendship.FriendshipResponse
import com.backend.ripple.dto.friendship.UserSummaryResponse
import com.backend.ripple.friendship.service.FriendshipService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/friendship")
class FriendshipController (private val friendshipService: FriendshipService) {
    @PostMapping("/request/{receiverId}")
    fun sendRequest(@PathVariable receiverId: Long): ResponseEntity<FriendshipResponse>
    {
        val friendship =friendshipService.sendRequest(receiverId);
        return ResponseEntity.ok(friendship)
    }
    @PutMapping("/accept/{senderId}")
    fun acceptRequest(@PathVariable senderId: Long): ResponseEntity<FriendshipResponse>
    {
        val friendship =friendshipService.acceptRequest(senderId)
        return ResponseEntity.ok(friendship)
    }
    @DeleteMapping("/reject/{senderId}")
    fun rejectRequest(@PathVariable senderId: Long): ResponseEntity<Void>
    {
        friendshipService.rejectRequest(senderId)
        return ResponseEntity.noContent().build()
    }
    @DeleteMapping("/remove/{userId}")
    fun removeRequest(@PathVariable userId: Long): ResponseEntity<Void>
    {
        friendshipService.removeFriendship(userId)
        return ResponseEntity.noContent().build()
    }
    @GetMapping("/friends")
    fun friends(): ResponseEntity<List<FriendshipResponse>>
    {
        val allFriendship = friendshipService.getFriendships()
        return ResponseEntity.ok(allFriendship)
    }
    @GetMapping("/pending")
    fun getPendingFriendships(): ResponseEntity<List<FriendshipResponse>>
    {
        val pending = friendshipService.getPendingRequests()
        return ResponseEntity.ok(pending)
    }
    @GetMapping("/sent")
    fun getSentFriendships(): ResponseEntity<List<FriendshipResponse>>
    {
        val sentFriendship = friendshipService.getRequestsSent()
        return ResponseEntity.ok(sentFriendship)
    }
    @GetMapping("/{userId}/friends")
    fun getFriendsList(@PathVariable userId: Long): ResponseEntity<List<UserSummaryResponse>> {
        val friends = friendshipService.getFriends(userId)
        return ResponseEntity.ok(friends)
    }

}