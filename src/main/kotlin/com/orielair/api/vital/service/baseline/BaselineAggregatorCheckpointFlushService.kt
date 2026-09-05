package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.persistence.repository.baseline.BaselineAggregatorCheckpointRepository
import com.orielair.api.vital.valueObject.baseline.AggregationWindow
import org.springframework.stereotype.Service

@Service
class BaselineAggregatorCheckpointFlushService(
    private val registry: BaselineAggregatorRegistry,
    private val checkpointService: BaselineAggregatorCheckointService,
    private val repository: BaselineAggregatorCheckpointRepository
) {
    // Persists checkpoints in a database
    fun flushCheckpoints( window: AggregationWindow){

        // Convert the live aggregator into a persistence-ready checkpoint.
        val checkpoints = checkpointService.createCheckpoints(
            registry = registry,
            windowStart = window.start,
            windowEnd = window.end
        )
        // persists the checkpoints
        repository.saveAll(checkpoints)

    }
}