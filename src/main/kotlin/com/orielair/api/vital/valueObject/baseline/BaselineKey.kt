package com.orielair.api.vital.valueObject.baseline

import com.orielair.api.vital.enum.baseline.ObservationType
import com.orielair.api.vital.enum.baseline.PhysiologicalState
import java.util.UUID

// This uniquely identifies each BaselineAggregator in the in-memory registry (BaselineAggregatorRegistry)
data class BaselineKey(
    val userId: UUID, // UUID later,
    val observationType: ObservationType,
    val physiologicalState: PhysiologicalState
)