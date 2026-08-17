package com.orielair.api.analytics.persistence.dto

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID


@Component
class VitalHistoryRecordDto(
    var id: UUID? = null,
    var userId: UUID? = null,
    var vitalType: String? = null,
    var value: Double? = null,
    var unit: String? = null,
    var physiologicalState: String? = null,
    var sourceEventID: UUID? = null,
    var correlationId: UUID? = null,
    var ingestedAt: Instant? = null,
    var updatedAt: Instant? = null,
)