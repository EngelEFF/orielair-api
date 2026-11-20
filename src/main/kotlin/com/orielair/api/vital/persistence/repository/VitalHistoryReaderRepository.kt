package com.orielair.api.vital.persistence.repository

import com.orielair.api.vital.enum.baseline.ObservationType
import com.orielair.api.vital.port.VitalHistoryReader
import com.orielair.api.vital.port.VitalHistoryRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class VitalHistoryReaderRepository(
    private val vitalHistoryRepo: VitalHistoryRepository
): VitalHistoryReader {
    override fun readVitalsHistory(userId: UUID, from: Instant, to: Instant, pageable: Pageable): Page<VitalHistoryRecord> {
        val entities = vitalHistoryRepo.findAllByUserIdAndIngestedAtBetween(userId, from, to, pageable)
        return entities.map { VitalHistoryRecord(
            id = it.id,
            vitalId = it.vitalId,
            userId = it.userId,
            windowId = it.windowId,

            observationType = it.observationType,
            value = it.value,
            unit = it.unit,
            timestamp = it.timestamp,
            physiologicalState = it.physiologicalState,

            ingestedAt = it.ingestedAt,
        )
        }
    }

    override fun readVitalHistory(
        userId: UUID,
        observationType: ObservationType,
        from: Instant,
        to: Instant,
        pageable: Pageable
    ): Page<VitalHistoryRecord> {
        val entities = vitalHistoryRepo.findAllByUserIdAndObservationTypeAndIngestedAtBetween(userId, observationType, from, to, pageable )
        return entities.map { VitalHistoryRecord(
            id = it.id,
            vitalId = it.vitalId,
            userId = it.userId,
            windowId = it.windowId,

            observationType = it.observationType,
            value = it.value,
            unit = it.unit,
            timestamp = it.timestamp,
            physiologicalState = it.physiologicalState,

            ingestedAt = it.ingestedAt,
        )
        }
    }

}
