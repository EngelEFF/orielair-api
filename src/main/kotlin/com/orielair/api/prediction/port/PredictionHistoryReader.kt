package com.orielair.api.prediction.port

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

interface PredictionHistoryReader {
    fun readPredictionHistory(userId: UUID, from: Instant, to: Instant, pageable: Pageable): Page<PredictionHistoryRecord>
}