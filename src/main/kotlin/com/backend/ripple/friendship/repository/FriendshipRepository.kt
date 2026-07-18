package com.backend.ripple.friendship.repository

import com.backend.ripple.model.friendship.Friendship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FriendshipRepository : JpaRepository<Friendship, Long> {
    fun existsBySender_UserIdAndReceiver_UserId(senderId: Long, receiverId: Long): Boolean
    fun findBySender_UserIdAndReceiver_UserId(senderId: Long, receiverId: Long): Optional<Friendship>
    fun findBySender_UserIdAndStatus(senderId: Long, status: Int): List<Friendship>
    fun findByReceiver_UserIdAndStatus(receiverId: Long, status: Int): List<Friendship>
    @Query("SELECT f FROM Friendship f WHERE (f.sender.userId = :userId OR f.receiver.userId = :userId) AND f.status = 2")
    fun findAllFriends(@Param("userId") userId: Long): List<Friendship>
}