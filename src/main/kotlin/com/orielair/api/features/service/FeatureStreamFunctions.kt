package com.orielair.api.features.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import com.orielair.api.shared.event.VitalRecorded
import java.util.function.Consumer
import org.springframework.messaging.Message

// features/services/FeatureStreamFunctions.kt
@Configuration
class FeatureStreamFunctions(
    private val accumulator: VitalEventAccumulator,
) {

    @Bean
    fun accumulateShortHorizonEvents(): Consumer<Message<List<VitalRecorded>>> =
        Consumer { message ->
            accumulator.accumulateShortHorizon(message)
        }

    @Bean
    fun accumulateLongHorizonEvents(): Consumer<Message<List<VitalRecorded>>> =
        Consumer { message ->
            accumulator.accumulateLongHorizon(message)
        }


}

