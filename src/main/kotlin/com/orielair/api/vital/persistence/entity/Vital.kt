package com.orielair.api.vital.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import java.time.Instant

@Entity
@Table(name = "vitals")
class Vital (
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
     var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var userId: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var heartRate: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var respiratoryRate: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var heartRateVariability: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var physiologicalState: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var correlationId: String? = null,

    @Column(columnDefinition = "TIMESTAMP")
    var ingestedAt: Instant? = null,

    @Column(columnDefinition = "TIMESTAMP")
    var updatedAt: Instant? = null,
    )