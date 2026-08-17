package com.orielair.api.prediction.persistence.repository

import com.orielair.api.prediction.persistence.entity.PredictionHistory
import com.orielair.api.prediction.port.PredictionHistoryRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface PredictionHistoryRepository: JpaRepository<PredictionHistory, UUID>, PagingAndSortingRepository<PredictionHistory, UUID> {
    fun findAllByUserIdAndCreatedAtBetween(userId: UUID, from: Instant, to: Instant , pageable: Pageable): Page<PredictionHistory>
}