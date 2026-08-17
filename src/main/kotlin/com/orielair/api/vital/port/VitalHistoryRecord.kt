package com.orielair.api.vital.port

import java.time.Instant
import java.util.UUID

data class VitalHistoryRecord(
    val id: UUID? = null,
    val userId: UUID,
    val vitalType: String,
    val value: Double,
    val unit: String,
    val physiologicalState: String,
    val sourceEventID: UUID?,
    val correlationId: UUID?,
    val ingestedAt: Instant,
    val updatedAt: Instant,
    )
