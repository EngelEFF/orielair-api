package com.orielair.api.vital.persistence.repository.baseline

import com.orielair.api.vital.persistence.entity.baseline.BaselineAggregatorCheckpoint
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface BaselineAggregatorCheckpointRepository: JpaRepository<BaselineAggregatorCheckpoint, UUID> {
    fun findByWindowStartGreaterThanEqualAndWindowEndLessThanEqual(
        start: Instant,
        end: Instant
    ): List<BaselineAggregatorCheckpoint> //make it page later
}