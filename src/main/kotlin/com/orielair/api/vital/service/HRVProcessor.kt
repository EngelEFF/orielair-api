package com.orielair.api.vital.service

import org.springframework.stereotype.Component

@Component
class HRVProcessor {

    @Suppress("UNUSED_PARAMETER")
    fun process(
        classifiedObservationBatch: ClassifiedObservationBatch
    ): List<RefinedObservation> {

        return listOf(
            RefinedObservation(
                type = "HEART_RATE_VARIABILITY",
                value = 45,
                unit = "ms",
                status = 1,
                timestamp = 1793059201000L
            )

        )
    }
}