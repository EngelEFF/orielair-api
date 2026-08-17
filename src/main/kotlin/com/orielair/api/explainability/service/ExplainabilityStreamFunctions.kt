package com.orielair.api.explainability.service

import com.orielair.api.shared.event.ExplainabilityGenerated
import com.orielair.api.shared.event.PredictionGenerated
import com.orielair.api.shared.event.FeatureComputed
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant


@Configuration
class ExplainabilityStreamFunctions {

    // This method listens and reads generated prediction events and calls in the appropriate methods from the explainability services to derive, persist and publish explainability to model inferences
    @Bean
    fun generateExplainability(): (PredictionGenerated) -> ExplainabilityGenerated = { prediction ->
        ExplainabilityGenerated(
            id = "1",
            userId = prediction.userId,
            predictionId = prediction.id,
            explanation = "PredictionHistory #${prediction.id} due to the rise in heart rate and respiratory over the last 48 hours",
            correlationId = prediction.correlationId,
            generatedAt = Instant.now(),
            updatedAt = null,
        )
    }
}