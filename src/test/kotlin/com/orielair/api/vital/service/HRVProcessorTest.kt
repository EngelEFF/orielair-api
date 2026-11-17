package com.orielair.api.vital.service

import com.orielair.api.vital.enum.baseline.PhysiologicalState
import com.orielair.api.vital.persistence.other.Metadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class HRVProcessorTest {
    private lateinit var hrvProcessor: HRVProcessor

    @BeforeEach
    fun setup() {
        // Arrange
         hrvProcessor = HRVProcessor()
    }

    @Test
    fun `process returns sample HRV observations`() {


        val classifiedBatch = ClassifiedObservationBatch(
            metadata = Metadata(
                bundleId = "vb-hrv-test-001",
                userId = "test-user-001",
                windowStart = 1793059200000L,
                windowEnd = 1793059260000L
            ),
            observationsByType = mapOf(
                "IBI" to listOf(
                    RefinedObservation(
                        type = "IBI",
                        value = 741,
                        unit = "ms",
                        status = 0,
                        timestamp = 1793059201000L
                    ),
                    RefinedObservation(
                        type = "IBI",
                        value = 753,
                        unit = "ms",
                        status = 0,
                        timestamp = 1793059230000L
                    )
                )
            ),
            physiologicalState = PhysiologicalState.AWAKE
        )

        val expectedObservations = listOf(
            RefinedObservation(
                type = "HRV",
                value = 45,
                unit = "ms",
                status = 1,
                timestamp = 1793059201000L
            ),
            RefinedObservation(
                type = "HRV",
                value = 48,
                unit = "ms",
                status = 1,
                timestamp = 1793059230000L
            )
        )

        // Act
        val result = hrvProcessor.process(classifiedBatch)

        // Assert
        assertEquals(expectedObservations, result)
    }
}