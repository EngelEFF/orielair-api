package com.orielair.api.vital.service
import com.orielair.api.vital.persistence.entity.Vital
import com.orielair.api.vital.persistence.repository.VitalRepository
import com.orielair.api.shared.event.VitalRecorded
import com.orielair.api.vital.persistence.entity.VitalHistory
import com.orielair.api.vital.persistence.other.VitalBundle
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
    private val vitalIngestionOrchestrator: VitalIngestionOrchestrator,
    private val baselineIngestionService: BaselineIngestionService,
) {

    fun store(vitalBundle: VitalBundle): List<Vital> {
        val vitalIngestionResult = vitalIngestionOrchestrator.orchestrate(vitalBundle)
        val savedVitals = vitalIngestionResult.vitals.map{ vitalRepository.save(it) }

        savedVitals.map{
            val baselineObservation =  BaselineObservation(
                userId = it.userId!!,
                observationType = it.observationType!!,
                physiologicalState = it.physiologicalState!!,
                timestamp = it.timestamp.toString(),
                value = it.value!!
            )

            // Ingest into baseline engine
            baselineIngestionService.ingest(baselineObservation)

            // Publishing can follow when ingestion is done or if we will wait and publish all at once too.
        }

        return savedVitals
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