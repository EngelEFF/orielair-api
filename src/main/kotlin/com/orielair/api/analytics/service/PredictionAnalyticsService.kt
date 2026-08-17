package com.orielair.api.analytics.service

import com.orielair.api.prediction.persistence.entity.PredictionHistory
import com.orielair.api.prediction.persistence.repository.PredictionHistoryReader
import com.orielair.api.prediction.port.PredictionHistoryRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID


@Service
class PredictionAnalyticsService(val predictionHistoryReader: PredictionHistoryReader,){

    fun readPredictionHistory(userId: UUID, from: Instant, to: Instant, pageable: Pageable): Page<PredictionHistoryRecord> {
       return predictionHistoryReader.readPredictionHistory(userId, from, to, pageable)
    }
}