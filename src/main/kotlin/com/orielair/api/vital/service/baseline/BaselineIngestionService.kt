package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.valueObject.baseline.BaselineKey
import com.orielair.api.vital.valueObject.baseline.BaselineObservation
import org.springframework.stereotype.Service

@Service
class BaselineIngestionService(
    // In order to create checkpoints, we will need the exact instance of registry used by this service
    private val registry: BaselineAggregatorRegistry
) {
    fun ingest(observation: BaselineObservation){

        // creates a baseline key which uniquely identifies each in memory baseline aggregator
        val baselineKey = BaselineKey(
            userId = observation.userId,
            observationType = observation.observationType,
            physiologicalState = observation.physiologicalState
        )

        // Retrieves or creates an aggregator for a specific vital state which belongs to a specific user        val aggregator = registry.getOrCreate(baselineKey)
        val aggregator = registry.getOrCreate(baselineKey)

        // Adds an observation to the aggregator.
        aggregator.add(observation.value)
    }
}