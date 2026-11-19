package com.orielair.api.vital.persistence.dto

import com.orielair.api.vital.enum.baseline.ObservationType
import com.orielair.api.vital.enum.baseline.PhysiologicalState
import jakarta.persistence.Column
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
data class VitalDto (
    var id : UUID? = null,
    var userId : String? = null,
    var correlationId: String? = null,

    var observationType: ObservationType? = null,
    var value: Int? = null,
    var unit: String? = null,
    var timestamp: Long? = null,

    var physiologicalState: PhysiologicalState? = null,
    var ingestedAt: Instant? = null,
    )