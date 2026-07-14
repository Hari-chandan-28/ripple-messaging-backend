package com.backend.ripple.dto.friendship

data class FriendshipResponse(
    val friendshipId: Long,
    val senderId: Long,
    val receiverId: Long,
    val status: Int
)