package com.orielair.api.vital.persistence.dto

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
data class VitalDto (
    var id : UUID? = null,
    var userId : String? = null,
    var heartRate : String? = null,
    var respiratoryRate: String? = null,
    var heartRateVariability: String? = null,
    var physiologicalState: String? = null,
    var correlationId: String? = null,
    var ingestedAt: Instant? = null,
    var updatedAt: Instant? = null
    )