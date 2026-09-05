package com.orielair.api.vital.valueObject.baseline

import com.orielair.api.vital.enum.baseline.ObservationType
import com.orielair.api.vital.enum.baseline.PhysiologicalState
import java.util.UUID

data class BaselineObservation (
    val userId: UUID,
    val observationType: ObservationType,
    val physiologicalState: PhysiologicalState,
    val timestamp: String,
    val value: Double,
)