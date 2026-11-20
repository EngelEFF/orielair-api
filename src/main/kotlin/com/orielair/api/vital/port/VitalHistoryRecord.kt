package com.orielair.api.vital.port

import com.orielair.api.vital.enum.baseline.ObservationType
import com.orielair.api.vital.enum.baseline.PhysiologicalState
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant
import java.util.UUID

data class VitalHistoryRecord(
    var id: UUID? = null,
    var vitalId: UUID? = null,
    var windowId: UUID? = null,
    var userId: UUID? = null,
    var observationType: ObservationType? = null,
    var value: Double? = null,
    var unit: String? = null,
    var timestamp: Long? = null,
    var physiologicalState: PhysiologicalState? = null,
    var ingestedAt: Instant? = null,
    )
