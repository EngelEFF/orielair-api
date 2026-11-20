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
    private val streamBridge: StreamBridge
) {

    fun store(vitalBundle: VitalBundle): List<Vital> {
        try {
            val vitalIngestionResult = vitalIngestionOrchestrator.orchestrate(vitalBundle)
            val savedVitals = vitalIngestionResult.vitals.map{ vitalRepository.save(it) }

            savedVitals.map{
                // Ingest into baseline engine
                ingestBaselineObservation(vital = it)

                // Publish it to Kafka
                publish(vital = it)

                // Stores the vitals as history
                storeVitalHistory(vital = it)
            }

            return savedVitals
        }
        catch(e: Exception){
            // Consider logging the error or throwing a domain-specific exception instead
            throw RuntimeException("Failed to store and process vital bundle", e)
        }
    }

    // This will be improved later now it is for testing the logic
    fun storeVitalHistory(vital: Vital): VitalHistory{
        val vitalHistory = VitalHistory(
            vitalId = vital.id,
            userId = vital.userId,
            windowId = vital.windowId,

            value = vital.value,
            unit = vital.unit,
            observationType = vital.observationType,
            timestamp = vital.timestamp,
            physiologicalState = vital.physiologicalState,

            ingestedAt = vital.ingestedAt,
        )
        return vitalHistoryRepository.save(vitalHistory)
    }

    fun read(id: UUID): Vital? {
        return vitalRepository.findByIdOrNull(id)
    }

    fun readAll(pageable: Pageable): Page<Vital> {
        return vitalRepository.findAll(pageable)
    }



    //@Transactional
    fun publish(vital: Vital){
        val vitalRecorded = VitalRecorded(
           id = vital.id,
           userId = vital.userId,
           correlationId = vital.windowId,

           observationType = vital.observationType,
           value = vital.value,
           unit = vital.unit,
           timestamp = vital.timestamp,
           physiologicalState = vital.physiologicalState,

           ingestedAt = vital.ingestedAt,
        )
        // Publish onto kafka using StreamBridge
        streamBridge.send("vitalsRecorded-out-0", vitalRecorded)
    }

    fun ingestBaselineObservation(vital: Vital){
        val baselineObservation = BaselineObservation(
            userId = vital.userId!!,
            observationType = vital.observationType!!,
            physiologicalState = vital.physiologicalState!!,
            timestamp = vital.timestamp.toString(),
            value = vital.value!!
        )
        baselineIngestionService.ingest(baselineObservation)
    }
}