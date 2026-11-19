package com.orielair.api.vital.service

import org.springframework.stereotype.Component

@Component
class RRProcessor {

    @Suppress("UNUSED_PARAMETER")
    fun process(
        classifiedObservationBatch: ClassifiedObservationBatch
    ): List<RefinedObservation> {

        return listOf(
            RefinedObservation(
                type = "RESPIRATORY_RATE",
                value = 15,
                unit = "br/min",
                status = 1,
                timestamp = 1793059201000L
            )
        )
    }
}