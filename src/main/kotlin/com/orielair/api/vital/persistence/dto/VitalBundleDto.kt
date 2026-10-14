package com.orielair.api.vital.persistence.dto

data class VitalBundleDto(
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
    val value: List<Int>,
    val unit: String,
    val status: List<Int>,
    val timestamp: Long? = null
)