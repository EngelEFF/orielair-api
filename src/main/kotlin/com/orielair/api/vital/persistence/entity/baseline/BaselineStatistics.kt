package com.orielair.api.vital.persistence.entity.baseline

import com.orielair.api.vital.enum.baseline.ObservationType
import com.orielair.api.vital.enum.baseline.PhysiologicalState
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "baseline_statistics")
class BaselineStatistics(

    @Id
    @GeneratedValue(GenerationType.AUTO)
    var id: UUID? = null,


    @Column(nullable = false)
    var userId: UUID? = null,

    @Enumerated(EnumType.STRING)
     @Column(nullable = false)
    var observationType: ObservationType? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var physiologicalState: PhysiologicalState? = null,

    // --- Parametric Measures (Exact from Moments) ---
    @Column(nullable = false)
    val stdDev: Double,

     @Column(nullable = false)
    var mean: Double? = null,

    @Column(nullable = false)
    var min: Double? = null,

    @Column(nullable = false)
    var max: Double? = null,

    // (Percentiles from KllQuantileSketch) ---
    @Column(nullable = false)
    val median: Double,  // P50

    @Column(nullable = false)
    val p01: Double,

    @Column(nullable = false)
    val p05: Double,

    @Column(nullable = false)
    val p25: Double,     // Q1

    @Column(nullable = false)
    val p75: Double,     // Q3

    @Column(nullable = false)
    val p95: Double,

    @Column(nullable = false)
    val p99: Double,

    // --- Robust Dispersion Measures ---
    @Column(nullable = false)
    val iqr: Double = p75 - p25,

    @Column(nullable = false)
    val madProxy: Double = 0.6745 * (p75 - p25), // Single-pass normal proxy for MAD

    @Column(nullable = false)
    var count: Long? = null,

    @CreationTimestamp
    var ingestedAt: Instant? = null,
)