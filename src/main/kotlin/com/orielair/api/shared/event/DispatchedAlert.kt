package com.orielair.api.shared.event

import java.time.Instant

data class DispatchedAlert (
    var id: String? = null, // It is supposed to be a UUID, but I've set it to a string for testing
    var userId: String? = null, // It is supposed to be a UUID, but I've set it to a string for testing
    var alertType: String? = null,
    var severity: String? = null,
    var triggeredAt: Instant? = null,
    var status: String? = null,
    var correlationId: String? = null, // It is supposed to be a UUID, but I've set it to a string for testing
    var updatedAt: Instant? = null,
)