
package com.orielair.api.features.service

import com.orielair.api.features.enum.PredictionHorizon
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class FeatureService(
    private val vitalEventAccumulator: VitalEventAccumulator
) {

    private companion object {
        val logger = LoggerFactory.getLogger(FeatureService::class.java)
    }

    /**
     * Receives notifications when user-specific batches
     * have been accumulated.
     *
     * Retrieves the corresponding batch from the accumulator
     * and makes it available for feature-processing evaluation.
     */
    @EventListener
    fun onVitalBatchAccumulated(event: VitalBatchAccumulatedEvent) {

        val batch = when (event.predictionHorizon) {

            PredictionHorizon.SHORT ->
                vitalEventAccumulator.getUserShortHorizonBatches(event.batchDeliveryId)[event.userId]

            PredictionHorizon.LONG ->
                vitalEventAccumulator.getUserLongHorizonBatches(event.batchDeliveryId)[event.userId]
        }

        if (batch == null) {
            logger.warn(
                "⚠️ [{} HORIZON] Accumulated batch could not be retrieved | " +
                        "userId={} | deliveryId={}",
                event.predictionHorizon,
                event.userId,
                event.batchDeliveryId
            )
            return
        }

        logger.info(
            "👷🍅📦[{} HORIZON] Batch received for feature evaluation | " +
                    "userId={} | deliveryId={} | records={}",
            event.predictionHorizon,
            event.userId,
            event.batchDeliveryId,
            batch.records.size
        )

        // Feature-processing eligibility decision follows here.
    }
}
