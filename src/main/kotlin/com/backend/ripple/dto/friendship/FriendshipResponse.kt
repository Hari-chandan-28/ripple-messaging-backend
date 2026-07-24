package com.backend.ripple.dto.friendship

data class FriendshipResponse(
    val friendshipId: Long,
    val senderId: Long,
    val receiverId: Long,
    val status: Int,
    val friendId: Long = 0,
    val friendUsername: String = "",
    val friendName: String? = null,
    val friendProfilePic: String? = null,
    val friendBio: String? = null,
    val friendRelationshipStatus: String? = null,
)