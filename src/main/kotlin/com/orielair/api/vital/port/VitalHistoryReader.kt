package com.orielair.api.vital.port

import com.orielair.api.vital.enum.baseline.ObservationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

interface VitalHistoryReader {
fun readVitalsHistory(userId: UUID, from: Instant, to: Instant, pageable: Pageable): Page<VitalHistoryRecord>

fun readVitalHistory(userId: UUID, observationType: ObservationType, from: Instant, to: Instant, pageable: Pageable): Page<VitalHistoryRecord>
}