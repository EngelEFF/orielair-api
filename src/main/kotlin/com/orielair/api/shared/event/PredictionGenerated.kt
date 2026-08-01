package com.orielair.api.shared.event

import java.time.Instant

class PredictionGenerated (
    var id: String? = null, // This is supposed to be uuid, but for the sake of testing I have made it a string.
    var userId: String? = null, // This is supposed to be uuid, but for the sake of testing, I have made it a string.
    var modelID: String? = null, // This is supposed to be uuid, but for the sake of testing, I have made it a string.
    var modelVersion: String? = null,
    var riskScore: Double? = null,
    var riskLevel: String? = null,
    var confidence: Double? = null,
    var predictedWindow: String? = null,
    var correlationId: String? = null, // This is supposed to be uuid, but for the sake of testing convenience I have made it a string.
    var predictedAt: Instant? = null,
    var updatedAt: Instant? = null,
)