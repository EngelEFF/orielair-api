package com.orielair.api.vital.service
import com.orielair.api.vital.persistence.entity.Vital
import com.orielair.api.vital.persistence.repository.VitalRepository
import com.orielair.api.shared.event.VitalRecorded
import com.orielair.api.vital.persistence.entity.VitalHistory
import com.orielair.api.vital.persistence.repository.VitalHistoryRepository
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
    private val streamBridge: StreamBridge,
    private val vitalHistoryRepository: VitalHistoryRepository
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

    @Transactional
    fun publishVital(vital: Vital): VitalRecorded {

        val vitalRecorded = VitalRecorded(
            id = vital.id,
            userId = vital.userId,
            heartRate = vital.heartRate,
            respiratoryRate = vital.respiratoryRate,
            heartRateVariability = vital.heartRateVariability,
            physiologicalState = vital.physiologicalState,
            correlationId = vital.correlationId,
            ingestedAt = vital.ingestedAt,
            updatedAt = vital.updatedAt,
        )

        // Publish onto Kafka via StreamBridge
        // "vitalsRecorded-out-0" maps directly to application.yml binding name
        streamBridge.send("vitalsRecorded-out-0", vitalRecorded)
        return vitalRecorded
    }

}