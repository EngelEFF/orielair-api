package com.orielair.api.shared.event

import java.time.Instant
import java.util.UUID

data class FeatureComputed (
    var id : String? = null, // It is supposed to be a UUID, but I've set it to a string for testing
    var userId: String? = null,
    var feature : String? = null, // supposed to be an object, but I've set it to string only for testing
    var modelVersion: String? = null,
    var physiologicalState: String? = null,
    var windowType: String? = null,
    var windowDuration: String? = null,
    var computedAt: Instant? = null,
    var correlationId: String? = null,
    var updatedAt: Instant? = null,
)