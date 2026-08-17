package com.orielair.api.prediction.port

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID


@Component
data class PredictionHistoryRecord (
    var id : UUID? = null,
    var userId : UUID? = null,
    var modelVersion: Double? = null,
    var riskScore: Double? = null,
    var confidence: Double? = null,
    var sourceEventId : UUID? = null,
    var correlationId : UUID? = null,
    var createdAt : Instant? = null,
    )