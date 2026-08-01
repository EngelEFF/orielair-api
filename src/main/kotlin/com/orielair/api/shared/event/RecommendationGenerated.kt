package com.orielair.api.shared.event

import java.time.Instant

data class RecommendationGenerated (
    var id: String? = null, // It is supposed to be a UUID, but I've set it to a string for testing
    var userId: String? = null, // It is supposed to be a UUID, but I've set it to a string for testing
    var predictionId : String? = null, // It is supposed to be a UUID, but I've set it to a string for testing
    var recommendation : String? = null,
    var correlationId: String? = null,
    var generatedAt: Instant? = Instant.now(),
    var updatedAt: Instant? = null
)