package com.backend.ripple.auth.repository

import com.backend.ripple.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface  UserRepository : JpaRepository<User, Long>{
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun existsByUserId(userId: Long): Boolean
    fun findByUserId(userId: Long): Optional<User>

}