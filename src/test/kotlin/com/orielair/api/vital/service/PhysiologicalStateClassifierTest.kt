package com.orielair.api.vital.service

import com.orielair.api.vital.enum.baseline.PhysiologicalState
import com.orielair.api.vital.persistence.other.Metadata
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import java.util.UUID
import kotlin.test.Test

class PhysiologicalStateClassifierTest {

    private lateinit var physiologicalStateClassifier: PhysiologicalStateClassifier


    @BeforeEach
    fun setUp() {
        physiologicalStateClassifier = PhysiologicalStateClassifier()
    }


    @Test
    fun `classify returns a valid physiological state`() {

        // Arrange
        val organizedBatch = OrganizedObservationBatch(
            metadata = Metadata(
                bundleId = UUID.fromString("0c309222-f87f-4ea0-ab8d-0812f00fb4cb"),
                userId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                windowStart = 1793059200000L,
                windowEnd = 1793059260000L
            ),
            observationsByType = mapOf(
                "HEART_RATE" to listOf(
                    RefinedObservation(
                        type = "HEART_RATE",
                        value = 72.0,
                        unit = "BPM",
                        status = 1,
                        timestamp = 1793059201000L
                    )
                )
            ),
            accelerometerSamples = listOf(
                AccelerometerSample(
                    x = 120.0,
                    y = -45.0,
                    z = 975.0,
                    timestamp = 1793059201000L
                )
            )
        )

        // Act
        val result = physiologicalStateClassifier.classify(organizedBatch)

        // Assert
        assertTrue(
            result == PhysiologicalState.AWAKE ||
                    result == PhysiologicalState.ASLEEP
        )
    }
}