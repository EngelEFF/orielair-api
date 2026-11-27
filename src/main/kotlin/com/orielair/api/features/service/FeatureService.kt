
package com.orielair.api.features.service

import com.orielair.api.features.enum.PredictionHorizon
import com.orielair.api.shared.event.VitalRecorded
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.UUID

// Import PredictionHorizon from its existing package.
// Import FeatureComputationOrchestrator from its existing package.


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

@Service
class FeatureService(
    private val vitalEventAccumulator: VitalEventAccumulator,

    //private val featureComputationOrchestrator: FeatureComputationOrchestrator,

    private val featureHorizonProperties: FeatureHorizonProperties
) {

    private companion object {
        val logger = LoggerFactory.getLogger(FeatureService::class.java)
    }
    /**
     * Receives accumulated batch notifications and initiates
     * feature computation when the batch meets the requirements.
     */
    @EventListener
    fun computeFeatures(event: VitalBatchAccumulatedEvent) {



        try {

            // Retrieve the specific batch using its prediction horizon,
            // delivery ID and user ID.
            val batch = when (event.predictionHorizon) {
                PredictionHorizon.SHORT -> vitalEventAccumulator.getUserShortHorizonBatches(event.batchDeliveryId)[event.userId]
                PredictionHorizon.LONG -> vitalEventAccumulator.getUserLongHorizonBatches(event.batchDeliveryId)[event.userId]
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

            // Ensure that the batch contains records.
            if (batch.records.isEmpty()) {
                logger.warn(
                    "⚠️ [{}] Empty batch cannot be processed | userId={} | deliveryId={}",
                    event.predictionHorizon,
                    event.userId,
                    event.batchDeliveryId
                )
                return
            }

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

            // Verify that all records belong to the expected user.
            if (batch.records.any { it.userId.toString() != event.userId }) {
                logger.warn(
                    "⚠️ [{}] Batch contains records belonging to another user | " +
                            "userId={} | deliveryId={}",
                    event.predictionHorizon,
                    event.userId,
                    event.batchDeliveryId
                )
                return
            }

            logger.info("Called ✅✅✅")

            // Verify that this batch is at the head of the user's FIFO queue.
            val nextBatch = when (event.predictionHorizon) {
                PredictionHorizon.SHORT -> vitalEventAccumulator.peekUserShortHorizonBatch(event.userId)
                PredictionHorizon.LONG -> vitalEventAccumulator.peekUserLongHorizonBatch(event.userId)
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


            // Obtain the configured minimum record count for this horizon.
            val minimumRecordCount = when (event.predictionHorizon) {
                PredictionHorizon.SHORT ->  featureHorizonProperties.short.minimumRecordCount
                PredictionHorizon.LONG ->  featureHorizonProperties.long.minimumRecordCount
            }

            // Compare the individual batch's record count against
            // the configured minimum.
            val receivedRecordCount = batch.records.size

            if (receivedRecordCount < minimumRecordCount) {
                logger.info(
                    "⏳ [{}] Batch does not meet the minimum record count | " +
                            "userId={} | deliveryId={} | receivedRecords={} | minimumRecords={}",
                    event.predictionHorizon,
                    event.userId,
                    event.batchDeliveryId,
                    receivedRecordCount,
                    minimumRecordCount
                )

                // TEMPORARY TEST: Simulate successful feature processing
// by removing the current FIFO head.
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

                logger.info(
                    "🧹 [{}] TEST: FIFO head removed successfully | " +
                            "userId={} | deliveryId={} | removedRecords={}",
                    event.predictionHorizon,
                    event.userId,
                    removedBatch.batchDeliveryId,
                    removedBatch.records.size
                )
                return
            }

            // Prepare the computation input.
            val computationInput = FeatureComputationInput(
                userId = userId,
                horizon = event.predictionHorizon,
                vitals = batch.records
            )

            logger.info(
                "✅📦 Feature computation input prepared | " +
                        "userId={} | horizon={} | deliveryId={} | " +
                        "records={} | minimumRecords={}",
                computationInput.userId,
                computationInput.horizon,
                event.batchDeliveryId,
                computationInput.vitals.size,
                minimumRecordCount
            )

            // TEMPORARY TEST: Simulate successful feature processing
// by removing the current FIFO head.
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

            logger.info(
                "🧹 [{}] TEST: FIFO head removed successfully | " +
                        "userId={} | deliveryId={} | removedRecords={}",
                event.predictionHorizon,
                event.userId,
                removedBatch.batchDeliveryId,
                removedBatch.records.size
            )
            // Delegate feature computation to the orchestrator.
            // featureComputationOrchestrator.computeFeatures(computationInput)
        }catch (e:Exception){
            logger.error("❌❌❌ Error Processing Batch received from Accumulator", e)
            throw e
        }

    }
}