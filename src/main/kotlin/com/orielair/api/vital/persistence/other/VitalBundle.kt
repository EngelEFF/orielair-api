package com.orielair.api.vital.persistence.other

data class VitalBundle(
    val metadata: Metadata,
    val observations: List<Observation>
)

data class Metadata(
    val bundleId: String,
    val userId: String,
    val windowStart: Long,
    val windowEnd: Long
)

data class Observation(
    val type: String,
    val values: List<Int>,
    val unit: String,
    val statuses: List<Int>,
    val timestamp: Long?
)