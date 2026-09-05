package com.orielair.api.vital.persistence.entity.baseline

import com.orielair.api.vital.enum.baseline.ObservationType
import com.orielair.api.vital.enum.baseline.PhysiologicalState
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp

import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "baseline_aggregator_checkpoint")
class BaselineAggregatorCheckpoint(

    @Id
    @GeneratedValue
    var id: UUID? = null,


    @Column(nullable = false)
    val userId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var observationType: ObservationType? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var physiologicalState: PhysiologicalState? = null,

    @Column(nullable = false)
    var windowStart: Instant? = null,

    @Column(nullable = false)
    var windowEnd: Instant? = null,

    @CreationTimestamp
    var flushedAt: Instant? = null,

    @Column(nullable = false)
    var momentCount: Long? = null,

    @Column(nullable = false)
    var momentMean: Double? = null,

    @Column(nullable = false)
    var momentM2: Double? = null,

    @Column(nullable = false)
    var momentMin: Double? = null,

    @Column(nullable = false)
    var momentMax: Double? = null,

    @Column(nullable = false)
    var quantileAlgorithm: String? = null,

    // Remove @Lob completely which caused reading problem with hibernate
    @Column(name = "quantile_state", columnDefinition = "bytea", nullable = false)
    var quantileState: ByteArray? = null

)