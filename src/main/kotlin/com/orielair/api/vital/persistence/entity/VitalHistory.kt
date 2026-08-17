package com.orielair.api.vital.persistence.entity

import jakarta.persistence.Entity
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
    var userId: UUID,
    var vitalType: String,
    var value: Double,
    var unit: String,
    var physiologicalState: String,
    var sourceEventID: UUID?,
    var correlationId: UUID?,
    var ingestedAt: Instant,
    var updatedAt: Instant,

    )