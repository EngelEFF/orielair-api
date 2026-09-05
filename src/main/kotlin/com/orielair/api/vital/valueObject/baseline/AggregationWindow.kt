package com.orielair.api.vital.valueObject.baseline

import java.time.Instant
// Stores the current window during checkpoint.
data class AggregationWindow(
    // Extract this to a separate file later.
    val start: Instant,
    val end: Instant
)

