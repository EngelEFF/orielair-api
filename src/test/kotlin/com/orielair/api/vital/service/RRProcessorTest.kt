//package com.orielair.api.vital.service
//
//import com.orielair.api.vital.enum.baseline.PhysiologicalState
//import com.orielair.api.vital.persistence.other.Metadata
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.BeforeEach
//import java.util.UUID
//import kotlin.test.Test
//
//class RRProcessorTest {
//
//    private lateinit var rrProcessor: RRProcessor
//
//    @BeforeEach
//    fun setUp() {
//        rrProcessor = RRProcessor()
//    }
//
//    @Test
//    fun `process returns sample respiratory rate observations`() {
//
//        // Arrange
//        val classifiedBatch = ClassifiedObservationBatch(
//            metadata = Metadata(
//                bundleId = UUID.fromString("0c309222-f87f-4ea0-ab8d-0812f00fb4cb"),
//                userId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
//                windowStart = 1793059200000L,
//                windowEnd = 1793059260000L
//            ),
//            observationsByType = mapOf(
//                "PPG_GREEN" to listOf(
//                    RefinedObservation(
//                        type = "PPG_GREEN",
//                        value = 1250.0,
//                        unit = "ADC",
//                        status = 0,
//                        timestamp = 1793059201000L
//                    ),
//                    RefinedObservation(
//                        type = "PPG_GREEN",
//                        value = 1240.0,
//                        unit = "ADC",
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
//                type = "RESPIRATORY_RATE",
//                value = 15.0,
//                unit = "br/min",
//                status = 1,
//                timestamp = 1793059201000L
//            ),
//            RefinedObservation(
//                type = "RESPIRATORY_RATE",
//                value = 16.0,
//                unit = "br/min",
//                status = 1,
//                timestamp = 1793059230000L
//            )
//        )
//
//        // Act
//        val result = rrProcessor.process(classifiedBatch)
//
//        // Assert
//        assertEquals(expectedObservations, result)
//    }
//}