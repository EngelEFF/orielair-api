package com.orielair.api.recommendation.service

import com.orielair.api.shared.event.PredictionGenerated
import com.orielair.api.shared.event.RecommendationGenerated
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant


@Configuration
class RecommendationStreamFunctions {

    // This method listens and reads prediction generated events and calls in the appropriate methods to derive, persist and publish recommendations for users based on model inference
    @Bean
    fun generateRecommendation(): (PredictionGenerated) -> RecommendationGenerated = { prediction ->
        RecommendationGenerated(
            id = "1",
            userId = prediction.userId,
            predictionId = prediction.id,
            recommendation = "Current adjust your physical activities to reduce the possibility of an impending asthma exacerbation",
            correlationId = prediction.correlationId,
            generatedAt = Instant.now(),
            updatedAt = null,
        )
    }
}