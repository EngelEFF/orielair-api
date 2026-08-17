package com.orielair.api.analytics.service

import com.orielair.api.vital.persistence.repository.VitalHistoryReaderRepository
import com.orielair.api.vital.port.VitalHistoryRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID


@Service
class VitalAnalyticsService (val vitalHistoryReaderRepository: VitalHistoryReaderRepository) {

    fun readVitalsHistory(userId: UUID, from: Instant, to: Instant, pageable: Pageable): Page<VitalHistoryRecord> {
        val vitals = vitalHistoryReaderRepository.readVitalsHistory(userId, from, to, pageable)
        return vitals
    }

    fun readVitalHistory(userId: UUID, vitalType: String, from: Instant, to: Instant, pageable: Pageable): Page<VitalHistoryRecord> {
        val vitals = vitalHistoryReaderRepository.readVitalHistory(userId, vitalType, from, to, pageable)
        return vitals
    }

}