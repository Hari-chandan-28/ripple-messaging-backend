package com.backend.ripple.profile

import com.backend.ripple.model.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProfileRepository : JpaRepository<Profile, Long> {
    fun findByUserId(userId: Long): Optional<Profile>
    fun existsByUserId(userId: Long): Boolean
}