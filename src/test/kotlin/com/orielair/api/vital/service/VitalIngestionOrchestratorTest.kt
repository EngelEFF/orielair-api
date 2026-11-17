import com.orielair.api.vital.`enum`.AcceptanceClassification
import com.orielair.api.vital.enum.baseline.PhysiologicalState
import com.orielair.api.vital.persistence.other.Metadata
import com.orielair.api.vital.persistence.other.Observation
import com.orielair.api.vital.persistence.other.VitalBundle
import com.orielair.api.vital.service.AccelerometerSample
import com.orielair.api.vital.service.ClassifiedObservationBatch
import com.orielair.api.vital.service.HRVProcessor
import com.orielair.api.vital.service.OrganizedObservationBatch
import com.orielair.api.vital.service.PhysiologicalStateClassifier
import com.orielair.api.vital.service.RRProcessor
import com.orielair.api.vital.service.RefinedObservation
import com.orielair.api.vital.service.VitalIngestionOrchestrator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import kotlin.test.Test


class VitalIngestionOrchestratorTest{
    // Inside VitalIngestionOrchestratorTest
    private lateinit var vitalIngestionOrchestrator: VitalIngestionOrchestrator
    private lateinit var physiologicalStateClassifier: PhysiologicalStateClassifier
    private lateinit var rrProcessor: RRProcessor
    private lateinit var hrvProcessor: HRVProcessor

    val vitalBundle = VitalBundle(
        metadata = Metadata(
            bundleId = "vb-filter-test-001",
            userId = "test-user-001",
            windowStart = 1793059200000L,
            windowEnd = 1793059260000L
        ),
        observations = listOf(

            // =====================================================
            // PPG_GREEN
            // =====================================================

            // GOOD: status 0 = normal
            Observation(
                type = "PPG_GREEN",
                values = listOf(1250),
                unit = "ADC",
                statuses = listOf(0),
                timestamp = 1793059201000L
            ),

            // BAD: status -1 = higher-priority sensor operating
            Observation(
                type = "PPG_GREEN",
                values = listOf(1240),
                unit = "ADC",
                statuses = listOf(-1),
                timestamp = 1793059202000L
            ),

            // UNSPECIFIED: unrecognized status
            Observation(
                type = "PPG_GREEN",
                values = listOf(1260),
                unit = "ADC",
                statuses = listOf(99),
                timestamp = 1793059203000L
            ),

            // MALFORMED: multiple values for a single-value observation
            Observation(
                type = "PPG_GREEN",
                values = listOf(1250, 1260),
                unit = "ADC",
                statuses = listOf(0),
                timestamp = 1793059204000L
            ),


            // =====================================================
            // HEART_RATE
            // =====================================================

            // GOOD: status 1 = successful measurement
            Observation(
                type = "HEART_RATE",
                values = listOf(72),
                unit = "BPM",
                statuses = listOf(1),
                timestamp = 1793059205000L
            ),

            // BAD: status 0 = initial measurement / higher-priority sensor
            Observation(
                type = "HEART_RATE",
                values = listOf(73),
                unit = "BPM",
                statuses = listOf(0),
                timestamp = 1793059206000L
            ),

            // BAD: status -2 = wearable movement
            Observation(
                type = "HEART_RATE",
                values = listOf(74),
                unit = "BPM",
                statuses = listOf(-2),
                timestamp = 1793059207000L
            ),

            // BAD: status -3 = wearable detached
            Observation(
                type = "HEART_RATE",
                values = listOf(75),
                unit = "BPM",
                statuses = listOf(-3),
                timestamp = 1793059208000L
            ),

            // BAD: status -8 = weak PPG / user moved
            Observation(
                type = "HEART_RATE",
                values = listOf(76),
                unit = "BPM",
                statuses = listOf(-8),
                timestamp = 1793059209000L
            ),

            // BAD: status -10 = weak PPG / excessive movement
            Observation(
                type = "HEART_RATE",
                values = listOf(77),
                unit = "BPM",
                statuses = listOf(-10),
                timestamp = 1793059210000L
            ),

            // BAD: status -999 = higher-priority sensor operating
            Observation(
                type = "HEART_RATE",
                values = listOf(78),
                unit = "BPM",
                statuses = listOf(-999),
                timestamp = 1793059211000L
            ),

            // UNSPECIFIED: unrecognized status
            Observation(
                type = "HEART_RATE",
                values = listOf(79),
                unit = "BPM",
                statuses = listOf(99),
                timestamp = 1793059212000L
            ),

            // MALFORMED: multiple values for a single-value observation
            Observation(
                type = "HEART_RATE",
                values = listOf(72, 73),
                unit = "BPM",
                statuses = listOf(1),
                timestamp = 1793059213000L
            ),


            // =====================================================
            // IBI
            // =====================================================

            // GOOD, BAD, UNSPECIFIED and zero-value validation.
            // Each value is paired with the status at the same index.
            Observation(
                type = "IBI",
                values = listOf(741, 753, 719, 1803, 0, 677),
                unit = "ms",
                statuses = listOf(0, 0, 0, -1, 0, 99),
                timestamp = 1793059214000L
            ),

            // MALFORMED: values and statuses have different lengths
            Observation(
                type = "IBI",
                values = listOf(741, 753, 719),
                unit = "ms",
                statuses = listOf(0, -1),
                timestamp = 1793059215000L
            ),

            // =====================================================
// ACCELEROMETER
// =====================================================

// GOOD: Three valid axis values (X, Y, Z).
// Statuses are intentionally empty.
            Observation(
                type = "ACCELEROMETER",
                values = listOf(120, -45, 975),
                unit = "m/s²",
                statuses = emptyList(),
                timestamp = 1793059216000L
            ),

// GOOD: Another valid accelerometer sample.
            Observation(
                type = "ACCELEROMETER",
                values = listOf(135, -52, 982),
                unit = "m/s²",
                statuses = emptyList(),
                timestamp = 1793059217000L
            ),

// GOOD: Another valid accelerometer sample.
            Observation(
                type = "ACCELEROMETER",
                values = listOf(110, -38, 968),
                unit = "m/s²",
                statuses = emptyList(),
                timestamp = 1793059218000L
            )


        )
    )

    @BeforeEach
    fun setUp() {
        physiologicalStateClassifier =
            Mockito.mock(PhysiologicalStateClassifier::class.java)

        rrProcessor = Mockito.mock(RRProcessor::class.java)
        hrvProcessor = Mockito.mock(HRVProcessor::class.java)

        vitalIngestionOrchestrator = VitalIngestionOrchestrator(
            physiologicalStateClassifier,
            rrProcessor,
            hrvProcessor
        )

    }

    @Test
    fun `filterObservations resolves all statuses and malformed observations`() {

        val result = vitalIngestionOrchestrator.filterObservations(vitalBundle)

        // Overall outcome counts
        assertEquals(5, result.accepted.refinedObservations.size)
        assertEquals(12, result.rejected.size)
        assertEquals(3, result.malformed.size)
        assertEquals(0, result.unsupported.size)

        // GOOD: PPG_GREEN and HEART_RATE
        assertEquals(
            1,
            result.accepted.refinedObservations.count { it.type == "PPG_GREEN" }
        )

        assertEquals(
            1,
            result.accepted.refinedObservations.count { it.type == "HEART_RATE" }
        )

        // Every recognized BAD HEART_RATE status is handled
        val expectedBadHeartRateStatuses = setOf(0, -2, -3, -8, -10, -999)

        val actualBadHeartRateStatuses = result.rejected
            .filter {
                it.observation.type == "HEART_RATE" &&
                        it.classification == AcceptanceClassification.BAD
            }
            .map { it.observation.status }
            .toSet()

        assertEquals(expectedBadHeartRateStatuses, actualBadHeartRateStatuses)

        // PPG_GREEN: BAD status -1
        assertTrue(
            result.rejected.any {
                it.observation.type == "PPG_GREEN" &&
                        it.observation.status == -1 &&
                        it.classification == AcceptanceClassification.BAD
            }
        )

        // UNSPECIFIED statuses for PPG_GREEN and HEART_RATE
        assertTrue(
            result.rejected.any {
                it.observation.type == "PPG_GREEN" &&
                        it.observation.status == 99 &&
                        it.classification == AcceptanceClassification.UNSPECIFIED
            }
        )

        assertTrue(
            result.rejected.any {
                it.observation.type == "HEART_RATE" &&
                        it.observation.status == 99 &&
                        it.classification == AcceptanceClassification.UNSPECIFIED
            }
        )

        // IBI: valid readings are accepted independently
        assertEquals(
            listOf(741, 753, 719),
            result.accepted.refinedObservations
                .filter { it.type == "IBI" }
                .map { it.value }
        )

        // IBI: status -1 is BAD
        assertTrue(
            result.rejected.any {
                it.observation.type == "IBI" &&
                        it.observation.value == 1803 &&
                        it.observation.status == -1 &&
                        it.classification == AcceptanceClassification.BAD
            }
        )

        // IBI: unknown status is UNSPECIFIED
        assertTrue(
            result.rejected.any {
                it.observation.type == "IBI" &&
                        it.observation.value == 677 &&
                        it.observation.status == 99 &&
                        it.classification == AcceptanceClassification.UNSPECIFIED
            }
        )

        // IBI: status is GOOD, but zero interval must still be rejected
        val zeroIntervalRejection = result.rejected.single {
            it.observation.type == "IBI" &&
                    it.observation.value == 0
        }

        assertEquals(
            AcceptanceClassification.GOOD,
            zeroIntervalRejection.classification
        )

        assertTrue(zeroIntervalRejection.reason.contains("zero"))

        // Malformed observations: one for each supported type
        assertEquals(
            setOf("PPG_GREEN", "HEART_RATE", "IBI"),
            result.malformed.map { it.observation.type }.toSet()
        )

        // Verify malformed diagnostics explain the structural failures
        assertTrue(
            result.malformed.any {
                it.observation.type == "PPG_GREEN" &&
                        it.reason.contains("values=2") &&
                        it.reason.contains("statuses=1")
            }
        )

        assertTrue(
            result.malformed.any {
                it.observation.type == "HEART_RATE" &&
                        it.reason.contains("values=2") &&
                        it.reason.contains("statuses=1")
            }
        )

        assertTrue(
            result.malformed.any {
                it.observation.type == "IBI" &&

                        it.reason.contains("values=3") &&
                        it.reason.contains("statuses=2")
            }
        )
    }


    @Test
    fun `filterObservations classifies unsupported observation types`() {
        val unsupportedObservation = Observation(
            type = "TEMPERATURE",
            values = listOf(36),
            unit = "°C",
            statuses = listOf(0),
            timestamp = 1793059201000L
        )

        val result = vitalIngestionOrchestrator.filterObservations(
            vitalBundle.copy(
                observations = listOf(unsupportedObservation)
            )
        )

        assertEquals(1, result.unsupported.size)
        assertEquals(
            unsupportedObservation,
            result.unsupported.single().observation
        )

        assertTrue(result.unsupported.single().reason.isNotBlank())

        assertTrue(result.accepted.refinedObservations.isEmpty())
        assertTrue(result.rejected.isEmpty())
        assertTrue(result.malformed.isEmpty())
    }


    @Test
    fun `filterObservations marks an empty IBI batch as malformed`() {
        val emptyIbiObservation = Observation(
            type = "IBI",
            values = emptyList(),
            unit = "ms",
            statuses = emptyList(),
            timestamp = 1793059201000L
        )

        val result = vitalIngestionOrchestrator.filterObservations(
            vitalBundle.copy(
                observations = listOf(emptyIbiObservation)
            )
        )

        assertEquals(1, result.malformed.size)
        assertEquals(
            emptyIbiObservation,
            result.malformed.single().observation
        )

        assertTrue(result.malformed.single().reason.isNotBlank())

        assertTrue(result.accepted.refinedObservations.isEmpty())
        assertTrue(result.rejected.isEmpty())
        assertTrue(result.unsupported.isEmpty())
    }

    @Test
    fun `filterObservations marks mismatched IBI values and statuses as malformed`() {
        val mismatchedIbiObservation = Observation(
            type = "IBI",
            values = listOf(741, 753, 719),
            unit = "ms",
            statuses = listOf(0, -1),
            timestamp = 1793059201000L
        )

        val result = vitalIngestionOrchestrator.filterObservations(
            vitalBundle.copy(
                observations = listOf(mismatchedIbiObservation)
            )
        )

        assertEquals(1, result.malformed.size)
        assertEquals(
            mismatchedIbiObservation,
            result.malformed.single().observation
        )

        val rejection = result.malformed.single()

        assertTrue(rejection.reason.isNotBlank())
        assertTrue(rejection.reason.contains("values=3"))
        assertTrue(rejection.reason.contains("statuses=2"))

        assertTrue(result.accepted.refinedObservations.isEmpty())
        assertTrue(result.rejected.isEmpty())
        assertTrue(result.unsupported.isEmpty())
    }

    @Test
    fun `filterObservations marks empty single-reading values or statuses as malformed`() {
        val emptyObservations = listOf(
            // HEART_RATE: empty values
            Observation(
                type = "HEART_RATE",
                values = emptyList(),
                unit = "BPM",
                statuses = listOf(1),
                timestamp = 1793059201000L
            ),

            // HEART_RATE: empty statuses
            Observation(
                type = "HEART_RATE",
                values = listOf(72),
                unit = "BPM",
                statuses = emptyList(),
                timestamp = 1793059202000L
            ),

            // HEART_RATE: both lists empty
            Observation(
                type = "HEART_RATE",
                values = emptyList(),
                unit = "BPM",
                statuses = emptyList(),
                timestamp = 1793059203000L
            ),

            // PPG_GREEN: empty values
            Observation(
                type = "PPG_GREEN",
                values = emptyList(),
                unit = "ADC",
                statuses = listOf(0),
                timestamp = 1793059204000L
            ),

            // PPG_GREEN: empty statuses
            Observation(
                type = "PPG_GREEN",
                values = listOf(1250),
                unit = "ADC",
                statuses = emptyList(),
                timestamp = 1793059205000L
            ),

            // PPG_GREEN: both lists empty
            Observation(
                type = "PPG_GREEN",
                values = emptyList(),
                unit = "ADC",
                statuses = emptyList(),
                timestamp = 1793059206000L
            )
        )

        val result = vitalIngestionOrchestrator.filterObservations(
            vitalBundle.copy(observations = emptyObservations)
        )

        assertEquals(6, result.malformed.size)

        assertEquals(
            emptyObservations.toSet(),
            result.malformed.map { it.observation }.toSet()
        )

        assertTrue(
            result.malformed.all {
                it.reason.contains("values=") &&
                        it.reason.contains("statuses=")
            }
        )

        assertTrue(result.accepted.refinedObservations.isEmpty())
        assertTrue(result.rejected.isEmpty())
        assertTrue(result.unsupported.isEmpty())
    }


    /*
    Test successful accelerometer extraction
This verifies that all three samples are accepted, their axes are correctly extracted, and no status information is required
     */

    @Test
    fun `filterObservations accepts and extracts accelerometer samples`() {

        val accelerometerObservations = vitalBundle.observations.filter {
            it.type == "ACCELEROMETER"
        }

        val result = vitalIngestionOrchestrator.filterObservations(
            vitalBundle.copy(
                observations = accelerometerObservations
            )
        )


        assertEquals(3, result.accepted.accelerometerSamples.size)

        assertEquals(
            listOf(
                AccelerometerSample(120, -45, 975, 1793059216000L),
                AccelerometerSample(135, -52, 982, 1793059217000L),
                AccelerometerSample(110, -38, 968, 1793059218000L)
            ),
            result.accepted.accelerometerSamples
        )

        assertTrue(result.accepted.refinedObservations.isEmpty())
        assertTrue(result.rejected.isEmpty())
        assertTrue(result.malformed.isEmpty())
        assertTrue(result.unsupported.isEmpty())
    }

    /*
    Test malformed accelerometer axis collections
    An accelerometer sample must contain exactly three axis values.
    Let's test both insufficient and excessive values.
     */
    @Test
    fun `filterObservations marks invalid accelerometer axis counts as malformed`() {

        val malformedObservations = listOf(

            // MALFORMED: Missing Z axis.
            Observation(
                type = "ACCELEROMETER",
                values = listOf(120, -45),
                unit = "m/s²",
                statuses = emptyList(),
                timestamp = 1793059216000L
            ),

            // MALFORMED: Unexpected fourth axis value.
            Observation(
                type = "ACCELEROMETER",
                values = listOf(120, -45, 975, 100),
                unit = "m/s²",
                statuses = emptyList(),
                timestamp = 1793059217000L
            )
        )

        val result = vitalIngestionOrchestrator.filterObservations(
            vitalBundle.copy(
                observations = malformedObservations
            )
        )

        assertEquals(2, result.malformed.size)

        assertEquals(
            malformedObservations,
            result.malformed.map { it.observation }
        )

        assertTrue(result.malformed.all { it.reason.isNotBlank() })

        assertTrue(result.accepted.accelerometerSamples.isEmpty())
        assertTrue(result.accepted.accelerometerSamples.isEmpty())
        assertTrue(result.rejected.isEmpty())
        assertTrue(result.unsupported.isEmpty())
    }


    /*
    Since AccelerometerSample requires a timestamp, a missing timestamp should also result in a malformed observation
     */
    @Test
    fun `filterObservations marks accelerometer samples without timestamps as malformed`() {

        val observation = Observation(
            type = "ACCELEROMETER",
            values = listOf(120, -45, 975),
            unit = "m/s²",
            statuses = emptyList(),
            timestamp = null
        )

        val result = vitalIngestionOrchestrator.filterObservations(
            vitalBundle.copy(
                observations = listOf(observation)
            )
        )

        assertEquals(1, result.malformed.size)

        assertEquals(
            observation,
            result.malformed.single().observation
        )

        assertTrue(result.malformed.single().reason.isNotBlank())

        assertTrue(result.accepted.accelerometerSamples.isEmpty())
        assertTrue(result.accepted.accelerometerSamples.isEmpty())
        assertTrue(result.rejected.isEmpty())
        assertTrue(result.unsupported.isEmpty())
    }












/*
Groups observations by type and preserves order
This is our primary test. We'll deliberately interleave observation types to verify that groupBy() places them in the correct collections without changing their relative order
*/
    @Test
    fun `organizeObservations groups observations by type and preserves their order`() {

        val metadata = vitalBundle.metadata

        val acceptedObservations = listOf(
            RefinedObservation("HEART_RATE", 72, "BPM", 1, 1000L),
            RefinedObservation("IBI", 741, "ms", 0, 1000L),
            RefinedObservation("PPG_GREEN", 1250, "ADC", 0, 1000L),
            RefinedObservation("HEART_RATE", 75, "BPM", 1, 2000L),
            RefinedObservation("IBI", 753, "ms", 0, 2000L),
            RefinedObservation("HEART_RATE", 78, "BPM", 1, 3000L),
            RefinedObservation("PPG_GREEN", 1240, "ADC", 0, 2000L)
        )

        val result = vitalIngestionOrchestrator.organizeObservations(
            metadata = metadata,
            acceptedObservations = acceptedObservations,
            accelerometerSamples = emptyList(),
        )

        // Verify metadata preservation
        assertEquals(metadata, result.metadata)

        // Verify all observation types were grouped
        assertEquals(
            setOf("HEART_RATE", "IBI", "PPG_GREEN"),
            result.observationsByType.keys
        )


        // Verify HEART_RATE grouping and ordering
        assertEquals(
            listOf(72, 75, 78),
            result.observationsByType["HEART_RATE"]?.map { it.value }
        )

        // Verify IBI grouping and ordering
        assertEquals(
            listOf(741, 753),
            result.observationsByType["IBI"]?.map { it.value }
        )

        // Verify PPG_GREEN grouping and ordering
        assertEquals(
            listOf(1250, 1240),
            result.observationsByType["PPG_GREEN"]?.map { it.value }
        )
    }


    /*
    Handles observations belonging to a single type
This verifies that the method doesn't introduce unnecessary groups or empty collections.
     */
    @Test
    fun `organizeObservations handles observations of a single type`() {

        val acceptedObservations = listOf(
            RefinedObservation("IBI", 741, "ms", 0, 1000L),
            RefinedObservation("IBI", 753, "ms", 0, 2000L),
            RefinedObservation("IBI", 719, "ms", 0, 3000L)
        )

        val result = vitalIngestionOrchestrator.organizeObservations(
            metadata = vitalBundle.metadata,
            acceptedObservations = acceptedObservations,
            accelerometerSamples = emptyList()

        )

        assertEquals(
            setOf("IBI"),
            result.observationsByType.keys
        )

        assertEquals(
            acceptedObservations,
            result.observationsByType["IBI"]
        )

        assertEquals(vitalBundle.metadata, result.metadata)
    }

/*
Handles an empty observation collection
This verifies that the method handles an empty input without throwing an exception and still preserves the bundle metadata
 */

    @Test
    fun `organizeObservations handles an empty observation collection`() {

        val result = vitalIngestionOrchestrator.organizeObservations(
            metadata = vitalBundle.metadata,
            acceptedObservations = emptyList(),
            accelerometerSamples = emptyList(),
        )

        assertTrue(result.observationsByType.isEmpty())

        assertEquals(vitalBundle.metadata, result.metadata)
    }




    @Test
    fun `classifyPhysiologicalState returns classified observation batch`() {

        // Arrange
        val physiologicalStateClassifier =
            Mockito.mock(PhysiologicalStateClassifier::class.java)

        val metadata = Metadata(
            bundleId = "vb-classification-test-001",
            userId = "test-user-001",
            windowStart = 1793059200000L,
            windowEnd = 1793059260000L
        )

        val observationsByType = mapOf(
            "HEART_RATE" to listOf(
                RefinedObservation(
                    type = "HEART_RATE",
                    value = 72,
                    unit = "BPM",
                    status = 1,
                    timestamp = 1793059201000L
                )
            ),
            "IBI" to listOf(
                RefinedObservation(
                    type = "IBI",
                    value = 741,
                    unit = "ms",
                    status = 0,
                    timestamp = 1793059202000L
                )
            )
        )

        val accelerometerSamples = listOf(
            AccelerometerSample(
                x = 120,
                y = -45,
                z = 975,
                timestamp = 1793059201000L
            )
        )

        val organizedBatch = OrganizedObservationBatch(
            metadata = metadata,
            observationsByType = observationsByType,
            accelerometerSamples = accelerometerSamples
        )

        val expectedState = PhysiologicalState.AWAKE

        Mockito.`when`(physiologicalStateClassifier.classify(organizedBatch))
            .thenReturn(expectedState)

        // Act
        val classifiedBatch = vitalIngestionOrchestrator.classifyPhysiologicalState(organizedBatch)

        // Assert
        assertEquals(metadata, classifiedBatch.metadata)

        assertEquals(observationsByType, classifiedBatch.observationsByType)

        assertEquals(expectedState, classifiedBatch.physiologicalState)


    }


    @Test
    fun `dispatchProcessing returns processed observation batch`() {

        // Arrange
        val metadata = Metadata(
            bundleId = "vb-dispatch-test-001",
            userId = "test-user-001",
            windowStart = 1793059200000L,
            windowEnd = 1793059260000L
        )

        val heartRateObservations = listOf(
            RefinedObservation(
                type = "HEART_RATE",
                value = 72,
                unit = "BPM",
                status = 1,
                timestamp = 1793059201000L
            )
        )

        val ibiObservations = listOf(
            RefinedObservation(
                type = "IBI",
                value = 741,
                unit = "ms",
                status = 0,
                timestamp = 1793059202000L
            )
        )

        val ppgObservations = listOf(
            RefinedObservation(
                type = "PPG_GREEN",
                value = 1250,
                unit = "ADC",
                status = 0,
                timestamp = 1793059203000L
            )
        )

        val observationsByType = mapOf(
            "HEART_RATE" to heartRateObservations,
            "IBI" to ibiObservations,
            "PPG_GREEN" to ppgObservations
        )

        val expectedState = PhysiologicalState.AWAKE

        val classifiedBatch = ClassifiedObservationBatch(
            metadata = metadata,
            observationsByType = observationsByType,
            physiologicalState = expectedState
        )

        val expectedRespiratoryRateObservations = listOf(
            RefinedObservation(
                type = "RESPIRATORY_RATE",
                value = 15,
                unit = "br/min",
                status = 1,
                timestamp = 1793059201000L
            ),
            RefinedObservation(
                type = "RESPIRATORY_RATE",
                value = 16,
                unit = "br/min",
                status = 1,
                timestamp = 1793059230000L
            )
        )

        val expectedHRVObservations = listOf(
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

        Mockito.`when`(rrProcessor.process(classifiedBatch))
            .thenReturn(expectedRespiratoryRateObservations)

        Mockito.`when`(hrvProcessor.process(classifiedBatch))
            .thenReturn(expectedHRVObservations)

        // Act
        val processedBatch =
            vitalIngestionOrchestrator.dispatchProcessing(classifiedBatch)

        // Assert
        assertEquals(metadata, processedBatch.metadata)

        assertEquals(
            expectedState,
            processedBatch.physiologicalState
        )

        // Original observations that were not processed are retained.
        assertEquals(
            heartRateObservations,
            processedBatch.observationsByType["HEART_RATE"]
        )

        // Processor outputs replace their respective input observation groups.
        assertEquals(
            expectedRespiratoryRateObservations,
            processedBatch.observationsByType["RESPIRATORY_RATE"]
        )

        assertEquals(
            expectedHRVObservations,
            processedBatch.observationsByType["HRV"]
        )

        // Original PPG_GREEN and IBI groups are no longer present.
        assertEquals(
            setOf("HEART_RATE", "RESPIRATORY_RATE", "HRV"),
            processedBatch.observationsByType.keys
        )

        // Verify that both processors received the classified batch.
        Mockito.verify(rrProcessor).process(classifiedBatch)
        Mockito.verify(hrvProcessor).process(classifiedBatch)
    }
}








