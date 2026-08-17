package com.orielair.api.vital.persistence.repository

import com.orielair.api.vital.persistence.entity.VitalHistory
import com.orielair.api.vital.port.VitalHistoryRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.List
import java.util.UUID

@Repository
interface VitalHistoryRepository: JpaRepository<VitalHistory, UUID>,
    PagingAndSortingRepository<VitalHistory, UUID> {

    // Transforms these to return pageables instead of ordinary lists
    fun findAllByUserIdAndIngestedAtBetween(
        userId: UUID,
        from: Instant,
        to: Instant,
        pageable: Pageable
    ): Page<VitalHistory>

    // Add for specific vital history tracking (e.g., Heart Rate charts)
    fun findAllByUserIdAndVitalTypeAndIngestedAtBetween(
        userId: UUID,
        vitalType: String,
        from: Instant,
        to: Instant,
        pageable: Pageable
    ): Page<VitalHistory>

}