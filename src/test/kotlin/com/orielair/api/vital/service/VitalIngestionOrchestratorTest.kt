import com.orielair.api.vital.`enum`.AcceptanceClassification
import com.orielair.api.vital.persistence.other.Metadata
import com.orielair.api.vital.persistence.other.Observation
import com.orielair.api.vital.persistence.other.VitalBundle
import com.orielair.api.vital.service.VitalIngestionOrchestrator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test


class VitalIngestionOrchestratorTest{
    // Inside VitalIngestionOrchestratorTest
    private lateinit var vitalIngestionOrchestrator: VitalIngestionOrchestrator

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
            )
        )
    )

    @BeforeEach
    fun setUp() {
        vitalIngestionOrchestrator = VitalIngestionOrchestrator()
    }

    @Test
    fun `filterObservations resolves all statuses and malformed observations`() {

        val result = vitalIngestionOrchestrator.filterObservations(vitalBundle)

        // Overall outcome counts
        assertEquals(5, result.accepted.size)
        assertEquals(12, result.rejected.size)
        assertEquals(3, result.malformed.size)
        assertEquals(0, result.unsupported.size)

        // GOOD: PPG_GREEN and HEART_RATE
        assertEquals(
            1,
            result.accepted.count { it.type == "PPG_GREEN" }
        )

        assertEquals(
            1,
            result.accepted.count { it.type == "HEART_RATE" }
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
            result.accepted
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

        assertTrue(result.accepted.isEmpty())
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

        assertTrue(result.accepted.isEmpty())
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

        assertTrue(result.accepted.isEmpty())
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

        assertTrue(result.accepted.isEmpty())
        assertTrue(result.rejected.isEmpty())
        assertTrue(result.unsupported.isEmpty())
    }
}








