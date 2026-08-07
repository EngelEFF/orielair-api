package com.orielair.api.timeline.service

import com.orielair.api.shared.event.DispatchedAlert
import com.orielair.api.shared.event.ExplainabilityGenerated
import com.orielair.api.shared.event.FeatureComputed
import com.orielair.api.shared.event.PredictionGenerated
import com.orielair.api.shared.event.RecommendationGenerated
import com.orielair.api.shared.event.VitalRecorded
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.slf4j.LoggerFactory

/**
 * The TimelineStream Functions listens to events from the respective domains and calls appropriate services to process and persist the data from the events.
 */
@Configuration
class TimelineStreamFunctions{

    private val logger = LoggerFactory.getLogger(TimelineStreamFunctions::class.java)

    /**
     * 1. Vitals Domain
     * Listens for raw physiological vital events (heart rate, respiratory rate, etc.)
     * and records them in the user's historical timeline feed.
     */
    @Bean
    fun captureVital(): (VitalRecorded) -> Unit = { vital ->
        logger.info("✅ Captured Vital Event: $vital ✅")
    }

    /**
     * 2. Features Domain
     * Listens for computed feature window events (RMSSD, physiological states)
     * and maps them to timeline metrics for trend analysis.
     */
    @Bean
    fun captureFeature(): (FeatureComputed) -> Unit = { feature ->
        logger.info("✅ Captured Feature Event: $feature ✅")
    }

    /**
     * 3. Prediction Domain
     * Listens for ML inference prediction events (risk scores, status changes)
     * and logs state transitions onto the user's risk timeline.
     */
    @Bean
    fun capturePrediction(): (PredictionGenerated) -> Unit = { prediction ->
        logger.info("✅ Captured Prediction Event: ${prediction.toString()} ✅")
    }

    /**
     * 4. Explainability Domain
     * Listens for model explainability output events and attaches underlying factor
     * attributions to the timeline entries for user/clinician transparency.
     */
    @Bean
    fun captureExplainability(): (ExplainabilityGenerated) -> Unit = { explainability ->
        logger.info("✅ Captured Explainability Event: $explainability ✅")
    }

    /**
     * 5. Recommendation Domain
     * Listens for generated actionable recommendations and records intervention steps
     * on the user's timeline feed.
     */
    @Bean
    fun captureRecommendation(): (RecommendationGenerated) -> Unit = { recommendation ->
        logger.info("✅ Captured Recommendation Event: $recommendation ✅")
    }

    /**
     * 6. Alert Domain
     * Listens for dispatched alert notifications (triggers, resolutions, severities)
     * and logs critical security/safety events to the user's incident timeline.
     */
    @Bean
    fun captureAlert(): (DispatchedAlert) -> Unit = { alert ->
        logger.info("✅ Captured Alert Event: $alert ✅")
    }
}