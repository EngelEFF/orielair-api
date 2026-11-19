package com.orielair.api.vital.persistence.other

import java.util.UUID

data class VitalBundle(
    val metadata: Metadata,
    val observations: List<Observation>
)

data class Metadata(
    val bundleId: UUID,
    val userId: UUID,
    val windowStart: Long,
    val windowEnd: Long
)

data class Observation(
    val type: String,
    val values: List<Double>,
    val unit: String,
    val statuses: List<Int>,
    val timestamp: Long?
)