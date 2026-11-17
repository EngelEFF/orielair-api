package com.orielair.api.vital.service

import com.orielair.api.vital.enum.baseline.PhysiologicalState
import com.orielair.api.vital.persistence.other.Metadata
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
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
                bundleId = "vb-classification-test-001",
                userId = "test-user-001",
                windowStart = 1793059200000L,
                windowEnd = 1793059260000L
            ),
            observationsByType = mapOf(
                "HEART_RATE" to listOf(
                    RefinedObservation(
                        type = "HEART_RATE",
                        value = 72,
                        unit = "BPM",
                        status = 1,
                        timestamp = 1793059201000L
                    )
                )
            ),
            accelerometerSamples = listOf(
                AccelerometerSample(
                    x = 120,
                    y = -45,
                    z = 975,
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