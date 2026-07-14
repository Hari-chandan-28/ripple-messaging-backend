package com.backend.ripple.dto.group

data class GroupResponse (
    val groupId: Long,
    val name: String,
    val description: String?,
    val userName:String,
    )