package com.backend.ripple.message.repository

import com.backend.ripple.model.MessageDelete
import com.backend.ripple.model.MessageDeleteId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageDeleteRepository : JpaRepository<MessageDelete, MessageDeleteId> {
}