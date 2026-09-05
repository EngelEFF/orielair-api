package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.persistence.entity.baseline.BaselineAggregatorCheckpoint
import com.orielair.api.vital.persistence.entity.baseline.BaselineStatistics
import com.orielair.api.vital.valueObject.baseline.BaselineKey
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.text.get

@Service
class BaselineComputationService(
    private val baselineAggregatorRestoreService: BaselineAggregatorRestoreService
) {

    fun computeBaselineStatistics(
        checkpoints: List<BaselineAggregatorCheckpoint>
    ): List<BaselineStatistics> {

        /*
         * The registry is used as a temporary in-memory aggregation space.
         *
         * Each unique BaselineKey:
         *     (userId, observationType, physiologicalState)
         *
         * maps to exactly one BaselineAggregator.
         *
         * This allows multiple x-hour checkpoints belonging to the same
         * baseline category to be restored and merged together.
         */
        val registry = BaselineAggregatorRegistry()

        /*
         * Each checkpoint represents the persisted state of a
         * BaselineAggregator for a specific baseline key and time window.
         *
         * The goal here is NOT to calculate statistics immediately.
         * Instead, we:
         *
         * 1. Reconstruct the BaselineKey.
         * 2. Restore the BaselineAggregator from its persisted state.
         * 3. Find or create the corresponding in-memory aggregator.
         * 4. Merge the restored aggregator into it.
         *
         * If multiple checkpoints have the same BaselineKey, their
         * statistical states will therefore accumulate into the same
         * in-memory aggregator.
         */
        checkpoints.forEach { checkpoint ->

            /*
             * Reconstruct the identity of the baseline represented by
             * this checkpoint.
             *
             * The BaselineKey determines which in-memory aggregator
             * the restored state belongs to.
             */
            val baselineKey = BaselineKey(
                userId = checkpoint.userId
                    ?: error("Checkpoint userId is missing"),

                observationType = checkpoint.observationType
                    ?: error("Checkpoint observationType is missing"),

                physiologicalState = checkpoint.physiologicalState
                    ?: error("Checkpoint physiologicalState is missing")
            )

            /*
             * Restore the persisted computational state back into a
             * fully functional BaselineAggregator.
             *
             * The restored aggregator contains the MomentState and
             * KLL sketch required for further computation and merging.
             */
            val restoredAggregator = baselineAggregatorRestoreService.restoreBaselineAggregator(checkpoint)
            /*
             * Retrieve the aggregator already associated with this
             * BaselineKey, or create a new empty aggregator if this
             * is the first checkpoint encountered for the key.
             */
            val aggregator =
                registry.getOrCreate(baselineKey)

            /*
             * Merge the restored checkpoint state into the aggregator
             * belonging to this baseline key.
             *
             * For the first checkpoint, the empty aggregator becomes
             * initialized by the merge.
             *
             * For subsequent checkpoints with the same key, their
             * statistical states are combined with the existing state.
             */
            aggregator.merge(restoredAggregator)
        }

        /*
         * At this point, the registry contains one fully merged
         * BaselineAggregator for every unique BaselineKey.
         *
         * Each aggregator now represents the combined statistical
         * state of all checkpoints belonging to that:
         *
         *     user + observation type + physiological state
         *
         * combination.
         *
         * We can now materialize the final baseline statistics.
         */
        return registry.keys().map { key ->

            /*
             * Retrieve the fully merged aggregator for this baseline key.
             */
            val aggregator = registry.get(key)
                ?: error("No aggregator found for key: $key")

            /*
             * Compute the final baseline statistics once.
             *
             * buildStatistics() derives:
             * - mean
             * - standard deviation
             * - min / max
             * - median and percentiles
             * - IQR
             * - MAD proxy
             * - sample count
             *
             * These are derived from the merged MomentState and KLL state.
             */
            val statistics = aggregator.buildStatistics()

            /*
             * Create the final baseline statistics representation.
             *
             * This object contains the materialized values intended for
             * persistent storage and downstream consumption. It no longer
             * needs the internal MomentState or KLL sketch.
             */
            BaselineStatistics(
                userId = key.userId,
                observationType = key.observationType,
                physiologicalState = key.physiologicalState,

                // --- Parametric Measures (Exact from Moments) ---
                mean = statistics.mean,
                stdDev = statistics.stdDev,
                min = statistics.min,
                max = statistics.max,

                // --- Percentiles from KllQuantileSketch ---
                median = statistics.median,
                p01 = statistics.p01,
                p05 = statistics.p05,
                p25 = statistics.p25,
                p75 = statistics.p75,
                p95 = statistics.p95,
                p99 = statistics.p99,

                // --- Robust Dispersion Measures ---
                iqr = statistics.iqr,
                madProxy = statistics.madProxy,

                /*
                 * The sample count comes directly from the merged
                 * MomentState and represents the total number of
                 * observations represented by all merged checkpoints.
                 */
                count = statistics.samples,

                /*
                 * Timestamp indicating when this final baseline
                 * statistics object was computed.
                 */
                ingestedAt = Instant.now()
            )
        }
    }



}