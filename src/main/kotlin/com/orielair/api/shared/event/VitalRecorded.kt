package com.orielair.api.shared.event

import java.time.Instant
import java.util.UUID

data class VitalRecorded (
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

