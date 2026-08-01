package com.orielair.api.features.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import com.orielair.api.shared.event.FeatureComputed
import com.orielair.api.shared.event.VitalRecorded
import java.time.Instant

// features/services/FeatureStreamFunctions.kt
@Configuration
class FeatureStreamFunctions(
    //private val calculator: FeatureCalculatorService
) {
    // This method listens and reads vitalRecorded events and calls in the appropriate methods to derive , persist and publish features
    @Bean
    fun computeFeatures(): (VitalRecorded) -> FeatureComputed = { vital ->
        FeatureComputed(
            id = "1",
            userId = vital.userId,
            feature = "heartRate ${vital.heartRate} respiratoryRate ${vital.respiratoryRate} heartRateVariability ${vital.heartRateVariability}",
            modelVersion = "1.0.0",
            physiologicalState = "awake",
            windowType = "short",
            windowDuration = "1h",
            computedAt = Instant.now(),
            correlationId = vital.correlationId,
            updatedAt = null
        )
    }


}

