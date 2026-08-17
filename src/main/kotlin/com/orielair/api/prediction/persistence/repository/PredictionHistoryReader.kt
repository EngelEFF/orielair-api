package com.orielair.api.prediction.persistence.repository

import com.orielair.api.prediction.port.PredictionHistoryReader
import com.orielair.api.prediction.port.PredictionHistoryRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID


@Repository
class PredictionHistoryReader(val predictionHistoryRepository: PredictionHistoryRepository) : PredictionHistoryReader {

    override fun readPredictionHistory(userId: UUID, from: Instant, to: Instant, pageable: Pageable): Page<PredictionHistoryRecord> {
        val entities = predictionHistoryRepository.findAllByUserIdAndCreatedAtBetween(userId, from, to, pageable)
        return entities.map {
            PredictionHistoryRecord(
                id = it.id,
                userId = userId,
                modelVersion = it.modelVersion,
                riskScore = it.riskScore,
                confidence = it.confidence,
                sourceEventId = it.sourceEventId,
                correlationId = it.correlationId,
                createdAt = it.createdAt
            )
        }
    }
}