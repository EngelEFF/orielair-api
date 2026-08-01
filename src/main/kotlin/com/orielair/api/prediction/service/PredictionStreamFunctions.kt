package com.orielair.api.prediction.service

import com.orielair.api.shared.event.PredictionGenerated
import com.orielair.api.shared.event.FeatureComputed
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant


@Configuration
class PredictionStreamFunctions {

    // This method listens and reads feature computed events and calls in the appropriate methods to derive, persist and publish model inference(prediction)
    @Bean
    fun generatePrediction(): (FeatureComputed) -> PredictionGenerated = { feature ->
        PredictionGenerated(
            id = "1",
            userId = feature.userId,
            modelID = "1",
            modelVersion = feature.modelVersion,
            riskScore = 0.9,
            riskLevel = "High",
            confidence = 1.0,
            predictedWindow = "24h",
            correlationId = feature.correlationId,
            predictedAt = Instant.now(),
            updatedAt = null,
        )
    }
}