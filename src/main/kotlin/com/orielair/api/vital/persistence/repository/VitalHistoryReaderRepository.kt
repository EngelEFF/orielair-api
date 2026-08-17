package com.orielair.api.vital.persistence.repository

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
            userId = it.userId,
            vitalType = it.vitalType,
            value = it.value,
            unit = it.unit,
            physiologicalState = it.physiologicalState,
            sourceEventID = it.sourceEventID,
            correlationId = it.correlationId,
            ingestedAt = it.ingestedAt,
            updatedAt = it.updatedAt,
        )
        }
    }

    override fun readVitalHistory(
        userId: UUID,
        vitalType: String,
        from: Instant,
        to: Instant,
        pageable: Pageable
    ): Page<VitalHistoryRecord> {
        val entities = vitalHistoryRepo.findAllByUserIdAndVitalTypeAndIngestedAtBetween(userId, vitalType,from, to, pageable )
        return entities.map { VitalHistoryRecord(
            id = it.id,
            userId = it.userId,
            vitalType = it.vitalType,
            value = it.value,
            unit = it.unit,
            physiologicalState = it.physiologicalState,
            sourceEventID = it.sourceEventID,
            correlationId = it.correlationId,
            ingestedAt = it.ingestedAt,
            updatedAt = it.updatedAt,
        )
        }
    }

}
