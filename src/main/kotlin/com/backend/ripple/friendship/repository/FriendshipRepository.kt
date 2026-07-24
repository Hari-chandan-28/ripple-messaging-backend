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
    @Query(
        value = """
        SELECT
        f.friendship_id AS friendshipId,
        f.sender_id AS senderId,
        f.receiver_id AS receiverId,
        f.status AS status,
        f.sender_id AS friendId,
        u.username AS friendUsername,
        p.profile_pic AS profilePic
    FROM friendship f
    JOIN user u
        ON u.user_id = f.sender_id
    LEFT JOIN profile p
        ON p.user_id = f.sender_id
    WHERE f.receiver_id = :userId
        AND f.status = 1;
        """,
        nativeQuery = true)
    fun getPendingFriends(
        @Param("userId") userId: Long
    ): List<FriendshipResponseProjection>
}

interface FriendshipResponseProjection {
    val friendshipId: Long
    val senderId: Long
    val receiverId: Long
    val status: Int
    val friendId: Long
    val friendUsername: String
    val profilePic: String?
}