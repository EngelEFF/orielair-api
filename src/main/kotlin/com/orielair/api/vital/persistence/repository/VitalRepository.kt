package com.orielair.api.vital.persistence.repository

import com.orielair.api.vital.persistence.entity.Vital
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VitalRepository: JpaRepository<Vital, UUID>, PagingAndSortingRepository<Vital, UUID> {
}