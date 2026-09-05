package com.orielair.api.vital.valueObject.baseline

/**
 * Immutable DTO representing a projected statistical baseline over a time window
 * (e.g., 7-day rolling baseline) for a specific vital sign metric.
 */

data class BaselineStatisticsState(
    // --- Parametric Measures (Exact from Moments) ---
    val mean: Double,
    val stdDev: Double,
    val min: Double,
    val max: Double,

    // --- Non-Parametric Measures (Percentiles from KllQuantileSketch) ---
    val median: Double,  // P50
    val p01: Double,
    val p05: Double,
    val p25: Double,     // Q1
    val p75: Double,     // Q3
    val p95: Double,
    val p99: Double,

    // --- Robust Dispersion Measures ---
    val iqr: Double = p75 - p25,
    val madProxy: Double = 0.6745 * (p75 - p25), // Single-pass normal proxy for MAD

    // --- Metadata & Operational Context ---
    val samples: Long,
    //  val computedAt: Instant = Instant.now(),
)