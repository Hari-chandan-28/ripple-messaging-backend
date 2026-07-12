package com.backend.ripple.dto

import com.backend.ripple.model.User
import org.springframework.data.annotation.CreatedBy
import org.springframework.stereotype.Service

data class GroupResponse (
    val groupId: Long,
    val name: String,
    val description: String?,
    val userName:String,
    )
