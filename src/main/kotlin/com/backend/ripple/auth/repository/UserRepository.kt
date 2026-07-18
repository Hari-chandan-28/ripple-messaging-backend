package com.backend.ripple.auth.repository

import com.backend.ripple.model.auth.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface  UserRepository : JpaRepository<User, Long>{
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun existsByUserId(userId: Long): Boolean
    fun findByUserId(userId: Long): Optional<User>
    fun existsByUsername(username: String): Boolean
    fun findByUsernameContainingIgnoreCaseAndIsPrivate(username: String, isPrivate: Boolean): List<User>
}