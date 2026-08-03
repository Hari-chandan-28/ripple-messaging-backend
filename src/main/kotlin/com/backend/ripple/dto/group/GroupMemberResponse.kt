package com.backend.ripple.dto.group

import com.backend.ripple.group.GroupRole

data class GroupMemberResponse(
    val memberId: Long,
    val memberUsername: String,
    val memberName: String?,
    val memberProfilePic: String?,
    val role: String,
)