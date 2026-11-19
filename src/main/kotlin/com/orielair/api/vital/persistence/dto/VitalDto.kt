package com.orielair.api.vital.persistence.dto

import com.orielair.api.vital.enum.baseline.ObservationType
import com.orielair.api.vital.enum.baseline.PhysiologicalState
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
data class VitalDto (
    var id : UUID? = null,
    var userId : UUID? = null,
    var windowId: UUID? = null,

    var observationType: ObservationType? = null,
    var value: Double? = null,
    var unit: String? = null,
    var timestamp: Long? = null,

    var physiologicalState: PhysiologicalState? = null,
    var ingestedAt: Instant? = null,
    )