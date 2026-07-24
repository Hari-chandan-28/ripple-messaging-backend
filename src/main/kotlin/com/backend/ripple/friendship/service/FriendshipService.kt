package com.backend.ripple.friendship.service

import com.backend.ripple.AlreadyExistsException
import com.backend.ripple.PrivateUserException
import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.dto.friendship.FriendshipResponse
import com.backend.ripple.dto.friendship.UserSummaryResponse
import com.backend.ripple.friendship.repository.FriendshipRepository
import com.backend.ripple.model.friendship.Friendship
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FriendshipService(private val friendshipRepository: FriendshipRepository, private val userRepository: UserRepository) {

    fun sendRequest(receiverId: Long): FriendshipResponse {
        val senderId = SecurityContextHolder.getContext().authentication?.principal as Long
        if (friendshipRepository.existsBySender_UserIdAndReceiver_UserId(senderId, receiverId) ||
            friendshipRepository.existsBySender_UserIdAndReceiver_UserId(receiverId, senderId)) {
            throw AlreadyExistsException("Friendship already exists")
        }
        val sender = userRepository.findById(senderId).orElseThrow { ResourceNotFoundException("User with id $senderId does not exist") }
        val receiver= userRepository.findById(receiverId) .orElseThrow { ResourceNotFoundException("User with id $receiverId does not exist") }

            val newFriendship = Friendship(
                sender = sender,
                receiver = receiver,
                status = 1,
            )
        val friend = friendshipRepository.save(newFriendship)
        return FriendshipResponse(
            friendshipId = friend.friendshipId,
            senderId = friend.sender.userId,
            receiverId = friend.receiver.userId,
            status = friend.status,
        )
    }
    fun acceptRequest(senderId: Long): FriendshipResponse {
        val receiverId = SecurityContextHolder.getContext().authentication?.principal as Long
        val newFriendship = friendshipRepository.findBySender_UserIdAndReceiver_UserId(senderId, receiverId).orElseThrow(){
            ResourceNotFoundException("Friendship does not exist")
        }
        newFriendship.status = 2;
        val friend = friendshipRepository.save(newFriendship)
        return FriendshipResponse(
            friendshipId = friend.friendshipId,
            senderId = friend.sender.userId,
            receiverId = friend.receiver.userId,
            status = friend.status,
        )
    }
    fun rejectRequest(senderId: Long) {
        val receiverId = SecurityContextHolder.getContext().authentication?.principal as Long
        val friendship = friendshipRepository.findBySender_UserIdAndReceiver_UserId(senderId, receiverId).orElseThrow{
            ResourceNotFoundException("No friendship between $senderId and $receiverId ")
        }
        friendshipRepository.delete(friendship);
    }
    @Transactional(readOnly = true)
    fun getFriendships(): List<FriendshipResponse> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        return friendshipRepository.findAllFriends(userId).map { friendship ->
            val friend = if (friendship.sender.userId == userId) friendship.receiver else friendship.sender
            FriendshipResponse(
                friendshipId = friendship.friendshipId,
                senderId = friendship.sender.userId,
                receiverId = friendship.receiver.userId,
                status = friendship.status,
                friendId = friend.userId,
                friendUsername = friend.username,
            )
        }
    }
    @Transactional(readOnly = true)
    fun getPendingRequests(): List<FriendshipResponse> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        return friendshipRepository.findByReceiver_UserIdAndStatus(userId, 1).map { friendship ->
            FriendshipResponse(
                friendshipId = friendship.friendshipId,
                senderId = friendship.sender.userId,
                receiverId = friendship.receiver.userId,
                status = friendship.status,
                friendId = friendship.sender.userId,
                friendUsername = friendship.sender.username,
            )
        }
    }
    @Transactional(readOnly = true)
    fun getRequestsSent(): List<FriendshipResponse> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        return friendshipRepository.findBySender_UserIdAndStatus(userId, 1).map { friendship ->
            FriendshipResponse(
                friendshipId = friendship.friendshipId,
                senderId = friendship.sender.userId,
                receiverId = friendship.receiver.userId,
                status = friendship.status,
                friendId = friendship.receiver.userId,
                friendUsername = friendship.receiver.username,
            )
        }
    }
    fun removeFriendship(friendId: Long) {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        var friendship = friendshipRepository.findBySender_UserIdAndReceiver_UserId(userId, friendId)
        if (!friendship.isPresent) {
            friendship = friendshipRepository.findBySender_UserIdAndReceiver_UserId(friendId, userId)
        }
        friendshipRepository.delete(friendship.orElseThrow { ResourceNotFoundException("Friendship not found") })
    }

    fun getFriends(personId: Long): List<UserSummaryResponse> {
        val curId = SecurityContextHolder.getContext().authentication?.principal as Long
        val targetUser = userRepository.findById(personId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        if (curId != personId && targetUser.isPrivate) {
            val areFriends = friendshipRepository.existsBySender_UserIdAndReceiver_UserId(curId, personId) ||
                    friendshipRepository.existsBySender_UserIdAndReceiver_UserId(personId, curId)
            if (!areFriends) {
                throw PrivateUserException("User is private")
            }
        }
        val result = friendshipRepository.findAllFriends(personId)

        val friendshipList = result.mapNotNull { friendship ->
            val friend = if (friendship.sender.userId == personId)
                friendship.receiver
            else
                friendship.sender
            //This runs a DB query for every private friend in the list to check if you're connected.
            // If someone has 500 friends and 200 are private, that's 400 extra queries. For now it's fine — your app is small.
            // But keep this in mind as a future optimization.
            friend.takeIf {
                !it.isPrivate ||
                        friendshipRepository.existsBySender_UserIdAndReceiver_UserId(curId, it.userId) ||
                        friendshipRepository.existsBySender_UserIdAndReceiver_UserId(it.userId, curId)
            }?.let {
                UserSummaryResponse(
                    userId = it.userId,
                    username = it.username
                )
            }
        }
        return friendshipList
    }
}