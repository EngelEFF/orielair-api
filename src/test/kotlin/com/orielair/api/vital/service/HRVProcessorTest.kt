//package com.orielair.api.vital.service
//
//import com.orielair.api.vital.enum.baseline.PhysiologicalState
//import com.orielair.api.vital.persistence.other.Metadata
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.BeforeEach
//import java.util.UUID
//import kotlin.test.Test
//
//class HRVProcessorTest {
//    private lateinit var hrvProcessor: HRVProcessor
//
//    @BeforeEach
//    fun setup() {
//        // Arrange
//         hrvProcessor = HRVProcessor()
//    }
//
//    @Test
//    fun `process returns sample HRV observations`() {
//
//
//        val classifiedBatch = ClassifiedObservationBatch(
//            metadata = Metadata(
//                bundleId = UUID.fromString("0c309222-f87f-4ea0-ab8d-0812f00fb4cb"),
//                userId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
//                windowStart = 1793059200000L,
//                windowEnd = 1793059260000L
//            ),
//            observationsByType = mapOf(
//                "IBI" to listOf(
//                    RefinedObservation(
//                        type = "IBI",
//                        value = 741.0,
//                        unit = "ms",
//                        status = 0,
//                        timestamp = 1793059201000L
//                    ),
//                    RefinedObservation(
//                        type = "IBI",
//                        value = 753.0,
//                        unit = "ms",
//                        status = 0,
//                        timestamp = 1793059230000L
//                    )
//                )
//            ),
//            physiologicalState = PhysiologicalState.AWAKE
//        )
//
//        val expectedObservations = listOf(
//            RefinedObservation(
//                type = "HEART_RATE_VARIABILITY",
//                value = 45.0,
//                unit = "ms",
//                status = 1,
//                timestamp = 1793059201000L
//            ),
//            RefinedObservation(
//                type = "HEART_RATE_VARIABILITY",
//                value = 48.0,
//                unit = "ms",
//                status = 1,
//                timestamp = 1793059230000L
//            )
//        )
//
//        // Act
//        val result = hrvProcessor.process(classifiedBatch)
//
//        // Assert
//        assertEquals(expectedObservations, result)
//    }
//}