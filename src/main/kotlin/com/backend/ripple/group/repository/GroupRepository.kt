package com.backend.ripple.group.repository

import com.backend.ripple.model.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface GroupRepository : JpaRepository<Group, Long>{
    fun findByGroupId(groupId: Long): Optional<Group>
}