package com.orielair.api.alert.service

import com.orielair.api.shared.event.DispatchedAlert
import com.orielair.api.shared.event.PredictionGenerated
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant


@Configuration
class AlertStreamFunctions {

    // This method listens and reads generated prediction events and calls in the appropriate methods from the alert services to derive, persist and publish alerts for the patient
    @Bean
    fun dispatchAlert(): (PredictionGenerated) -> DispatchedAlert = { prediction ->
        DispatchedAlert(
            id = "1",
            userId = prediction.userId,
            alertType = "😫",
            severity = "Severe",
            status = "Resolved",
            correlationId = prediction.correlationId,
            triggeredAt = Instant.now(),
            updatedAt = null,
        )
    }
}