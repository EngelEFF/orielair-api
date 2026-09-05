package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.persistence.entity.baseline.BaselineAggregatorCheckpoint
import com.orielair.api.vital.valueObject.baseline.AggregationWindow
import com.orielair.api.vital.valueObject.baseline.BaselineKey
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.text.get

@Service
class BaselineAggregatorCheckointService {


    // creates an instance of a checkpoint
    fun createCheckpoint(
        key: BaselineKey,
        windowStart: Instant,
        windowEnd: Instant,
        aggregator: BaselineAggregator
    ): BaselineAggregatorCheckpoint {
        // creates snapshot of the aggregator state
        val aggregatorSnapshot = aggregator.snapshotBaselineAggregatorState()

        // creates and returns the baselineAggregator. If all is alright, it should persist it and return as a reference of success/error
        return BaselineAggregatorCheckpoint(
            userId = key.userId,
            observationType = key.observationType,
            physiologicalState = key.physiologicalState,

            windowStart = windowStart,
            windowEnd = windowEnd,

            momentCount = aggregatorSnapshot.moment.count,
            momentMean = aggregatorSnapshot.moment.mean,
            momentM2 = aggregatorSnapshot.moment.m2,
            momentMax = aggregatorSnapshot.moment.max,
            momentMin = aggregatorSnapshot.moment.min,

            quantileAlgorithm = aggregatorSnapshot.quantile.algorithm,
            quantileState = aggregatorSnapshot.quantile.data

        )

    }

    // creates a collection of checkpoints
    fun createCheckpoints(
        registry: BaselineAggregatorRegistry,
        windowStart: Instant,
        windowEnd: Instant,
    ): List<BaselineAggregatorCheckpoint> {

        return registry.keys().map { key ->

            // Retrieve the active aggregator associated with this key.
            val aggregator = registry.get(key)
                ?: error("No aggregator found for key: $key")

            // Convert the live aggregator into a persistence-ready checkpoint.
            createCheckpoint(
                key = key,
                windowStart = windowStart,
                windowEnd = windowEnd,
                aggregator = aggregator
            )
        }
    }

    // Persists checkpoints in a database
    fun flushCheckpoints(registry: BaselineAggregatorRegistry, window: AggregationWindow){

        // Convert the live aggregator into a persistence-ready checkpoint.
        val checkpoints = createCheckpoints(
            registry = registry,
            windowStart = window.start,
            windowEnd = window.end
        )

        // persists the checkpoints
        // checkpointRepository.saveAll(checkpoints)
    }



}