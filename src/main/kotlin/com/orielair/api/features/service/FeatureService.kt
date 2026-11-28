package com.orielair.api.features.service

import com.orielair.api.features.enum.PredictionHorizon
import com.orielair.api.shared.event.VitalRecorded
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.ArrayDeque
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// Import FeatureComputationOrchestrator from its existing package when it is enabled.

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "orielair.features.horizons")
data class FeatureHorizonProperties(
    val short: Horizon,
    val long: Horizon
) {
    data class Horizon(
        val minimumRecordCount: Int,
        val duration: Long
    )
}


data class FeatureComputationInput(
    val userId: UUID,
    val horizon: PredictionHorizon,
    val vitals: List<VitalRecorded>
)

private data class WaitingBufferKey(
    val userId: UUID,
    val horizon: PredictionHorizon
)

@Service
class FeatureService(
    private val vitalEventAccumulator: VitalEventAccumulator,
    // private val featureComputationOrchestrator: FeatureComputationOrchestrator,
    private val featureHorizonProperties: FeatureHorizonProperties
) {

    private companion object {
        val logger = LoggerFactory.getLogger(FeatureService::class.java)
    }

    /**
     * Maintains in-memory waiting buffers keyed by user and prediction horizon.
     *
     * Each buffer is an independent ArrayDeque that retains records until the
     * configured minimum record count is reached.
     *
     * ConcurrentHashMap protects the buffer registry, while access to each
     * individual deque is synchronized on that deque because ArrayDeque is
     * not thread-safe.
     *
     * This allows independent users and prediction horizons to be processed
     * concurrently without interfering with one another.
     */
    private val waitingBuffers =
        ConcurrentHashMap<WaitingBufferKey, ArrayDeque<VitalRecorded>>()

    /**
     * Receives accumulated batch notifications and initiates feature computation
     * when the records available for a user and prediction horizon meet the
     * configured minimum record count.
     */
    @EventListener
    fun computeFeatures(event: VitalBatchAccumulatedEvent) {

        // Convert the event's user ID into the domain UUID.
        val userId = try {
            UUID.fromString(event.userId)
        } catch (exception: IllegalArgumentException) {
            logger.warn(
                "⚠️ Invalid user ID in accumulated batch event | userId={} | deliveryId={}",
                event.userId,
                event.batchDeliveryId,
                exception
            )
            return
        }

        // Identify the waiting buffer for this user and prediction horizon.
        val bufferKey = WaitingBufferKey(
            userId = userId,
            horizon = event.predictionHorizon
        )

        val waitingBuffer = waitingBuffers.computeIfAbsent(bufferKey) {
            ArrayDeque()
        }

        /*
         * Synchronize on this buffer to serialize concurrent access to its records.
         *
         * Events sharing the same user and prediction horizon acquire the same
         * monitor lock, ensuring that only one event processes that buffer at a time.
         *
         * Events targeting different buffers have independent locks and can therefore
         * execute concurrently.
         */
        synchronized(waitingBuffer) {
            try {
                // Retrieve the specific batch using its delivery ID and user ID.
                val batch = when (event.predictionHorizon) {
                    PredictionHorizon.SHORT ->
                        vitalEventAccumulator
                            .getUserShortHorizonBatches(event.batchDeliveryId)[event.userId]

                    PredictionHorizon.LONG ->
                        vitalEventAccumulator
                            .getUserLongHorizonBatches(event.batchDeliveryId)[event.userId]
                }

                // Verify that the batch exists.
                if (batch == null) {
                    logger.warn(
                        "⚠️ [{}] Accumulated batch not found | userId={} | deliveryId={}",
                        event.predictionHorizon,
                        event.userId,
                        event.batchDeliveryId
                    )
                    return
                }

                // Verify that the retrieved batch matches the notification.
                if (batch.batchDeliveryId != event.batchDeliveryId) {
                    logger.warn(
                        "⚠️ [{}] Batch delivery ID mismatch | userId={} | expected={} | actual={}",
                        event.predictionHorizon,
                        event.userId,
                        event.batchDeliveryId,
                        batch.batchDeliveryId
                    )
                    return
                }

                // Verify that this delivery is currently at the head of the FIFO queue.
                val nextBatch = when (event.predictionHorizon) {
                    PredictionHorizon.SHORT ->
                        vitalEventAccumulator.peekUserShortHorizonBatch(event.userId)

                    PredictionHorizon.LONG ->
                        vitalEventAccumulator.peekUserLongHorizonBatch(event.userId)
                }

                if (nextBatch?.batchDeliveryId != event.batchDeliveryId) {
                    logger.warn(
                        "⏳ [{}] Batch is not next in the user's FIFO queue | " +
                                "userId={} | deliveryId={}",
                        event.predictionHorizon,
                        event.userId,
                        event.batchDeliveryId
                    )
                    return
                }

                // Discard completely empty deliveries so they cannot block the FIFO queue.
                if (batch.records.isEmpty()) {
                    when (event.predictionHorizon) {
                        PredictionHorizon.SHORT ->
                            vitalEventAccumulator.removeProcessedShortHorizonBatch(
                                event.userId,
                                event.batchDeliveryId
                            )

                        PredictionHorizon.LONG ->
                            vitalEventAccumulator.removeProcessedLongHorizonBatch(
                                event.userId,
                                event.batchDeliveryId
                            )
                    }

                    logger.info(
                        "🗑️ [{}] Empty delivery discarded | userId={} | deliveryId={}",
                        event.predictionHorizon,
                        event.userId,
                        event.batchDeliveryId
                    )
                    return
                }

                // Verify that every record belongs to the expected user.
                if (batch.records.any { it.userId != userId }) {
                    // Discard the malformed delivery so it cannot block the FIFO queue.
                    when (event.predictionHorizon) {
                        PredictionHorizon.SHORT ->
                            vitalEventAccumulator.removeProcessedShortHorizonBatch(
                                event.userId,
                                event.batchDeliveryId
                            )

                        PredictionHorizon.LONG ->
                            vitalEventAccumulator.removeProcessedLongHorizonBatch(
                                event.userId,
                                event.batchDeliveryId
                            )
                    }

                    logger.error(
                        "❌ [{}] Invalid delivery discarded: records belong to another user | " +
                                "userId={} | deliveryId={}",
                        event.predictionHorizon,
                        event.userId,
                        event.batchDeliveryId
                    )
                    return
                }

                // Obtain the configured minimum record count for this horizon.
                val minimumRecordCount = when (event.predictionHorizon) {
                    PredictionHorizon.SHORT ->
                        featureHorizonProperties.short.minimumRecordCount

                    PredictionHorizon.LONG ->
                        featureHorizonProperties.long.minimumRecordCount
                }

                // Determine the records available for the next computation attempt.
                val bufferedRecordCount = waitingBuffer.size
                val receivedRecordCount = batch.records.size
                val accumulatedRecordCount = bufferedRecordCount + receivedRecordCount

                // Check whether the combined records meet the minimum requirement.
                if (accumulatedRecordCount < minimumRecordCount) {
                    // Remove the current delivery from the accumulator's FIFO queue.
                    val removedBatch = when (event.predictionHorizon) {
                        PredictionHorizon.SHORT ->
                            vitalEventAccumulator.removeProcessedShortHorizonBatch(
                                event.userId,
                                event.batchDeliveryId
                            )

                        PredictionHorizon.LONG ->
                            vitalEventAccumulator.removeProcessedLongHorizonBatch(
                                event.userId,
                                event.batchDeliveryId
                            )
                    }

                    // Retain the delivery's records for the next computation attempt.
                    waitingBuffer.addAll(removedBatch.records)

                    logger.info(
                        "⏳ [{}] Insufficient records. Delivery retained in waiting buffer | " +
                                "userId={} | deliveryId={} | receivedRecords={} | " +
                                "previouslyBuffered={} | totalBuffered={} | minimumRecords={} | " +
                                "additionalRecordsRequired={}",
                        event.predictionHorizon,
                        event.userId,
                        event.batchDeliveryId,
                        receivedRecordCount,
                        bufferedRecordCount,
                        waitingBuffer.size,
                        minimumRecordCount,
                        minimumRecordCount - waitingBuffer.size
                    )
                    return
                }

                // The combined records now meet the minimum requirement.
                // Prepare the computation input without modifying the waiting buffer.
                val computationVitals = buildList {
                    addAll(waitingBuffer)
                    addAll(batch.records)
                }

                val computationInput = FeatureComputationInput(
                    userId = userId,
                    horizon = event.predictionHorizon,
                    vitals = computationVitals
                )

                logger.info(
                    "✅📦 Feature computation input prepared | " +
                            "userId={} | horizon={} | deliveryId={} | " +
                            "bufferedRecords={} | receivedRecords={} | " +
                            "totalRecords={} | minimumRecords={}",
                    computationInput.userId,
                    computationInput.horizon,
                    event.batchDeliveryId,
                    bufferedRecordCount,
                    receivedRecordCount,
                    computationInput.vitals.size,
                    minimumRecordCount
                )

                // Delegate feature computation to the orchestrator.
                // featureComputationOrchestrator.computeFeatures(computationInput)

                /*
                 * TEMPORARY TEST:
                 * Simulate successful feature processing by removing the current FIFO head.
                 *
                 * Once the orchestrator is enabled, removal and buffer clearing
                 * must happen only after successful computation.
                 */
                val removedBatch = when (event.predictionHorizon) {
                    PredictionHorizon.SHORT ->
                        vitalEventAccumulator.removeProcessedShortHorizonBatch(
                            event.userId,
                            event.batchDeliveryId
                        )

                    PredictionHorizon.LONG ->
                        vitalEventAccumulator.removeProcessedLongHorizonBatch(
                            event.userId,
                            event.batchDeliveryId
                        )
                }

                // Clear previously accumulated records after successful handling.
                waitingBuffer.clear()

                logger.info(
                    "🧹 [{}] FIFO head removed and waiting buffer cleared | " +
                            "userId={} | deliveryId={} | processedRecords={}",
                    event.predictionHorizon,
                    event.userId,
                    removedBatch.batchDeliveryId,
                    computationInput.vitals.size
                )

            } catch (exception: Exception) {
                logger.error(
                    "❌ Error processing accumulated batch | userId={} | horizon={} | deliveryId={}",
                    event.userId,
                    event.predictionHorizon,
                    event.batchDeliveryId,
                    exception
                )
                throw exception
            }
        }
    }
}