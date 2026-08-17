package com.orielair.api.prediction.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID


@Entity
@Table(name = "prediction_history")
class PredictionHistory (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id : UUID? = null,
    var userId : UUID? = null,
    var modelVersion: Double? = null,
    var riskScore : Double? = null,
    var confidence: Double? = null,
    var sourceEventId : UUID? = null,
    var correlationId : UUID? = null,
    var createdAt: Instant? = null,
    )