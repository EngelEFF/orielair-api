package com.orielair.api.user.persistence.repository

import com.orielair.api.user.persistence.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository: JpaRepository<User, UUID>, PagingAndSortingRepository<User, UUID> {
}