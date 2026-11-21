package com.orielair.api.vital.service
import com.orielair.api.vital.persistence.entity.Vital
import com.orielair.api.vital.persistence.repository.VitalRepository
import com.orielair.api.shared.event.VitalRecorded
import com.orielair.api.vital.persistence.entity.VitalHistory
import com.orielair.api.vital.persistence.other.VitalBundle
import com.orielair.api.vital.persistence.repository.VitalHistoryRepository
import com.orielair.api.vital.service.baseline.BaselineIngestionService
import com.orielair.api.vital.valueObject.baseline.BaselineObservation
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.UUID

@Service
class VitalService(
    private val vitalRepository: VitalRepository,
    private val vitalHistoryRepository: VitalHistoryRepository,
    private val vitalIngestionOrchestrator: VitalIngestionOrchestrator,
    private val baselineIngestionService: BaselineIngestionService,
    private val streamBridge: StreamBridge
) {

    private val logger = LoggerFactory.getLogger(VitalService::class.java)

    /**
     * Processes a VitalBundle by sequentially replacing the user's current physiological state.
     *
     * For each incoming Vital:
     * 1. Identifies the existing Vital matching the user and observation type.
     * 2. Removes the existing Vital, if present.
     * 3. Persists the incoming Vital with a new identity.
     * 4. Archives the persisted Vital in VitalHistory for historical traceability.
     * 5. Registers baseline ingestion for execution after transaction commit.
     * 6. Registers Kafka publication for execution after transaction commit.
     *
     * All database operations participate in a single transaction. If processing fails,
     * the transaction rolls back and neither baseline ingestion nor Kafka publication executes.
     *
     * @return the newly persisted current Vital records.
     */
    @Transactional
    fun store(vitalBundle: VitalBundle): List<Vital> {
        try {
            val incomingVitals =
                vitalIngestionOrchestrator.orchestrate(vitalBundle).vitals

            val savedVitals = mutableListOf<Vital>()

            incomingVitals.forEach { incomingVital ->

                // 1. Identify the existing current Vital.
                val userId = requireNotNull(incomingVital.userId)
                val observationType =
                    requireNotNull(incomingVital.observationType)

                val existingVital =
                    vitalRepository.findByUserIdAndObservationType(
                        userId = userId,
                        observationType = observationType
                    )

                // 2. Remove the existing Vital, if present.
                existingVital?.let {
                    vitalRepository.delete(it)
                    vitalRepository.flush()
                }

                // 3. Persist the incoming Vital with its own identity.
                val savedVital =
                    vitalRepository.saveAndFlush(incomingVital)

                // 4. Persist its historical representation.
                storeVitalHistory(vital = savedVital)

                // 5. Register baseline ingestion after transaction commit.
                ingestBaselineObservation(vital = savedVital)

                // 6. Register Kafka publication after transaction commit.
                publish(vital = savedVital)

                // 7. Collect the successfully processed Vital.
                savedVitals.add(savedVital)
            }

            return savedVitals

        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to store and process vital bundle",
                e
            )
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


    /**
     * Defers Kafka publication until the surrounding database transaction commits successfully.
     * If the transaction rolls back, the event is never published.
     */
    fun publish(vital: Vital) {
        val vitalRecorded = VitalRecorded(
            eventId =  UUID.randomUUID(),
            vitalId = vital.id,
            userId = vital.userId,
            correlationId = vital.windowId,

            observationType = vital.observationType,
            value = vital.value,
            unit = vital.unit,
            timestamp = vital.timestamp,
            physiologicalState = vital.physiologicalState,

            ingestedAt = vital.ingestedAt,
        )

        // Register a synchronization callback with the active transaction.
        // Kafka publication is deferred rather than executed immediately.
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {

                // Spring invokes this callback only after a successful database commit.
                // A rollback prevents this callback from executing.
                override fun afterCommit() {
                    try {
                        streamBridge.send(
                            "vitalsRecorded-out-0",
                            vitalRecorded
                        )
                    } catch (e: Exception) {
                        logger.error(
                            "Failed to ingest baseline observation for Vital: {}",
                            vital.id,
                            e
                        )
                    }
                }
            }
        )
    }


    /**
     * Defers baseline ingestion until the surrounding database transaction
     * successfully commits. If the transaction rolls back, ingestion is skipped.
     */
    fun ingestBaselineObservation(vital: Vital) {

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {

                // Execute baseline ingestion only after a successful commit.
                override fun afterCommit() {


                    try {
                        val baselineObservation = BaselineObservation(
                            userId = vital.userId!!,
                            observationType = vital.observationType!!,
                            physiologicalState = vital.physiologicalState!!,
                            timestamp = vital.timestamp.toString(),
                            value = vital.value!!
                        )
                        baselineIngestionService.ingest(baselineObservation)
                    } catch (e: Exception) {
                        logger.error(
                            "Failed to ingest baseline observation for Vital: {}",
                            vital.id,
                            e
                        )
                    }
                }
            }
        )
    }
}