package com.orielair.api.vital.persistence.dto

import java.util.UUID

data class VitalBundleDto(
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
    val value: List<Double>,
    val unit: String,
    val status: List<Int>,
    val timestamp: Long? = null
)