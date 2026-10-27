package com.orielair.api.vital.service
import com.orielair.api.vital.persistence.entity.Vital
import com.orielair.api.vital.persistence.repository.VitalRepository
import com.orielair.api.shared.event.VitalRecorded
import com.orielair.api.vital.persistence.entity.VitalHistory
import com.orielair.api.vital.persistence.repository.VitalHistoryRepository
import com.orielair.api.vital.service.baseline.BaselineIngestionService
import com.orielair.api.vital.valueObject.baseline.BaselineObservation
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class VitalService(
    private val vitalRepository: VitalRepository,
    private val vitalHistoryRepository: VitalHistoryRepository,
) {

    fun store(vital: Vital): Vital {
        return vitalRepository.save(vital)
    }

    // This will be improved later now it is for testing the logic
    fun storeVitalHistory(vitalHistory: VitalHistory): VitalHistory{
        return vitalHistoryRepository.save(vitalHistory)
    }

    fun read(id: UUID): Vital? {
        return vitalRepository.findByIdOrNull(id)
    }

    fun readAll(pageable: Pageable): Page<Vital> {
        return vitalRepository.findAll(pageable)
    }
}