package com.orielair.api.vital.persistence.entity

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
import java.time.Instant
import java.util.UUID


@Entity
@Table(name = "vital_history")
class VitalHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    // Need to know which vital it originated from.
    @Column(nullable = false)
    var vitalId: UUID? = null,

    // Which window that vital belonged to.
    @Column(nullable = false)
    var windowId: UUID? = null,

    @Column(nullable = false)
    var userId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    var observationType: ObservationType? = null,

    @Column(nullable = false)
    var value: Double? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var unit: String? = null,

    @Column
    var timestamp: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    var physiologicalState: PhysiologicalState? = null,

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    var ingestedAt: Instant? = null,

    )