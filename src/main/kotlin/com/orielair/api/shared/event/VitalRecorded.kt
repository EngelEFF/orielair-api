package com.orielair.api.shared.event

import com.orielair.api.vital.enum.baseline.ObservationType
import com.orielair.api.vital.enum.baseline.PhysiologicalState
import java.time.Instant
import java.util.UUID

data class VitalRecorded (
    var eventId: UUID? = null,
    var vitalId : UUID? = null,
    var userId : UUID? = null,
    var correlationId: UUID? = null,

    var observationType: ObservationType? = null,
    var value: Double? = null,
    var unit: String? = null,
    var timestamp: Long? = null,

    var physiologicalState: PhysiologicalState? = null,
    var ingestedAt: Instant? = null,
)

