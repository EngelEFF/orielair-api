package com.orielair.api.analytics.persistence.dto

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID


@Component
class PredictionHistoryRecordDto(
    var id: UUID? = null,
    var userId: UUID? = null,
    var modelVersion: Double? = null,
    var riskScore: Double? = null,
    var confidence: Double? = null,
    var sourceEventID: UUID? = null,
    var correlationId: UUID? = null,
    var createdAt: Instant? = null
)
