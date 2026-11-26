//package com.orielair.api.vital.service
//
//import com.orielair.api.vital.persistence.other.Metadata
//import com.orielair.api.vital.enum.baseline.ObservationType
//import com.orielair.api.vital.enum.baseline.PhysiologicalState
//import com.orielair.api.vital.persistence.entity.Vital
//import com.orielair.api.vital.persistence.other.Observation
//import com.orielair.api.vital.persistence.other.VitalBundle
//import com.orielair.api.vital.persistence.repository.VitalHistoryRepository
//import com.orielair.api.vital.persistence.repository.VitalRepository
//import io.mockk.every
//import io.mockk.mockk
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.BeforeEach
//import java.time.Instant
//import java.util.UUID
//import kotlin.test.Test
//
//
//class VitalServiceTest {
//
//    // 1. Declare the variables at the class level
//    private lateinit var vitalRepository: VitalRepository
//    private lateinit var vitalHistoryRepository: VitalHistoryRepository
//    private lateinit var vitalService: VitalService
//    private lateinit var vitalIngestionOrchestrator: VitalIngestionOrchestrator
//
//
//    @BeforeEach
//    fun setUp() {
//        vitalRepository = mockk<VitalRepository>()
//        vitalHistoryRepository = mockk<VitalHistoryRepository>()
//
//        vitalIngestionOrchestrator = mockk<VitalIngestionOrchestrator>()
//
//        vitalService = VitalService(vitalRepository, vitalHistoryRepository, vitalIngestionOrchestrator)
//    }
//
//    @AfterEach
//    fun tearDown() {} // needed for singletons
//
//
//    /**
//     * Mock the dependencies collaborating with VitalService:
//     * - Repositories: isolate the service from database operations.
//     * - VitalIngestionOrchestrator: isolate the service from the ingestion pipeline.
//     *
//     * Do not mock VitalService itself; it is the class under test.
//     * Do not mock domain/data objects (VitalBundle, Metadata, Observation,
//     * VitalIngestionResult, Vital); use real instances as test inputs and outputs.
//     *
//     * The orchestrator's internal dependencies (RRProcessor, HRVProcessor and
//     * PhysiologicalStateClassifier) are not needed here because the orchestrator
//     * is already mocked. Stub only dependency methods actually invoked by store().
//     */
//    @Test
//    fun `save method should return the persisted vital when it succeeds`() {
//
//        val vitalBundle = VitalBundle(
//            metadata = Metadata(
//                bundleId = "vb-filter-test-001",
//                userId = "test-user-001",
//                windowStart = 1793059200000L,
//                windowEnd = 1793059260000L
//            ),
//            observations = listOf(
//
//                // =====================================================
//                // PPG_GREEN
//                // =====================================================
//
//                // GOOD: status 0 = normal
//                Observation(
//                    type = "PPG_GREEN",
//                    values = listOf(1250),
//                    unit = "ADC",
//                    statuses = listOf(0),
//                    timestamp = 1793059201000L
//                ),
//
//                // BAD: status -1 = higher-priority sensor operating
//                Observation(
//                    type = "PPG_GREEN",
//                    values = listOf(1240),
//                    unit = "ADC",
//                    statuses = listOf(-1),
//                    timestamp = 1793059202000L
//                ),
//
//                // UNSPECIFIED: unrecognized status
//                Observation(
//                    type = "PPG_GREEN",
//                    values = listOf(1260),
//                    unit = "ADC",
//                    statuses = listOf(99),
//                    timestamp = 1793059203000L
//                ),
//
//                // MALFORMED: multiple values for a single-value observation
//                Observation(
//                    type = "PPG_GREEN",
//                    values = listOf(1250, 1260),
//                    unit = "ADC",
//                    statuses = listOf(0),
//                    timestamp = 1793059204000L
//                ),
//
//
//                // =====================================================
//                // HEART_RATE
//                // =====================================================
//
//                // GOOD: status 1 = successful measurement
//                Observation(
//                    type = "HEART_RATE",
//                    values = listOf(72),
//                    unit = "BPM",
//                    statuses = listOf(1),
//                    timestamp = 1793059205000L
//                ),
//
//                // BAD: status 0 = initial measurement / higher-priority sensor
//                Observation(
//                    type = "HEART_RATE",
//                    values = listOf(73),
//                    unit = "BPM",
//                    statuses = listOf(0),
//                    timestamp = 1793059206000L
//                ),
//
//                // BAD: status -2 = wearable movement
//                Observation(
//                    type = "HEART_RATE",
//                    values = listOf(74),
//                    unit = "BPM",
//                    statuses = listOf(-2),
//                    timestamp = 1793059207000L
//                ),
//
//                // BAD: status -3 = wearable detached
//                Observation(
//                    type = "HEART_RATE",
//                    values = listOf(75),
//                    unit = "BPM",
//                    statuses = listOf(-3),
//                    timestamp = 1793059208000L
//                ),
//
//                // BAD: status -8 = weak PPG / user moved
//                Observation(
//                    type = "HEART_RATE",
//                    values = listOf(76),
//                    unit = "BPM",
//                    statuses = listOf(-8),
//                    timestamp = 1793059209000L
//                ),
//
//                // BAD: status -10 = weak PPG / excessive movement
//                Observation(
//                    type = "HEART_RATE",
//                    values = listOf(77),
//                    unit = "BPM",
//                    statuses = listOf(-10),
//                    timestamp = 1793059210000L
//                ),
//
//                // BAD: status -999 = higher-priority sensor operating
//                Observation(
//                    type = "HEART_RATE",
//                    values = listOf(78),
//                    unit = "BPM",
//                    statuses = listOf(-999),
//                    timestamp = 1793059211000L
//                ),
//
//                // UNSPECIFIED: unrecognized status
//                Observation(
//                    type = "HEART_RATE",
//                    values = listOf(79),
//                    unit = "BPM",
//                    statuses = listOf(99),
//                    timestamp = 1793059212000L
//                ),
//
//                // MALFORMED: multiple values for a single-value observation
//                Observation(
//                    type = "HEART_RATE",
//                    values = listOf(72, 73),
//                    unit = "BPM",
//                    statuses = listOf(1),
//                    timestamp = 1793059213000L
//                ),
//
//
//                // =====================================================
//                // IBI
//                // =====================================================
//
//                // GOOD, BAD, UNSPECIFIED and zero-value validation.
//                // Each value is paired with the status at the same index.
//                Observation(
//                    type = "IBI",
//                    values = listOf(741, 753, 719, 1803, 0, 677),
//                    unit = "ms",
//                    statuses = listOf(0, 0, 0, -1, 0, 99),
//                    timestamp = 1793059214000L
//                ),
//
//                // MALFORMED: values and statuses have different lengths
//                Observation(
//                    type = "IBI",
//                    values = listOf(741, 753, 719),
//                    unit = "ms",
//                    statuses = listOf(0, -1),
//                    timestamp = 1793059215000L
//                ),
//
//                // =====================================================
//// ACCELEROMETER
//// =====================================================
//
//// GOOD: Three valid axis values (X, Y, Z).
//// Statuses are intentionally empty.
//                Observation(
//                    type = "ACCELEROMETER",
//                    values = listOf(120, -45, 975),
//                    unit = "m/s²",
//                    statuses = emptyList(),
//                    timestamp = 1793059216000L
//                ),
//
//// GOOD: Another valid accelerometer sample.
//                Observation(
//                    type = "ACCELEROMETER",
//                    values = listOf(135, -52, 982),
//                    unit = "m/s²",
//                    statuses = emptyList(),
//                    timestamp = 1793059217000L
//                ),
//
//// GOOD: Another valid accelerometer sample.
//                Observation(
//                    type = "ACCELEROMETER",
//                    values = listOf(110, -38, 968),
//                    unit = "m/s²",
//                    statuses = emptyList(),
//                    timestamp = 1793059218000L
//                )
//
//
//            )
//        )
//
//        val vital1 = Vital(id = UUID.fromString("df168f80-51ee-4652-a9f7-652d1b84af43"), "1", "user_1", observationType = ObservationType.HEART_RATE, value= 10, "beats/min", timestamp= 1793059218000L,
//            PhysiologicalState.AWAKE, Instant.now())
//
//        val vital2 = Vital(id = UUID.fromString("df168f80-51ee-4652-a9f7-652d1b84af43"), "1", "user_1", observationType = ObservationType.HEART_RATE, value= 10, "beats/min", timestamp= 1793059218000L,
//            PhysiologicalState.AWAKE, Instant.now())
//
//        val vitalList = listOf(vital1, vital2)
//
//        val vitalIngestionResult = VitalIngestionResult(
//            metadata = vitalBundle.metadata,
//                vitals = vitalList
//        )
//
//        every {
//            vitalIngestionOrchestrator.orchestrate(vitalBundle)
//        } returns vitalIngestionResult
//
//
//        // What the repository should do when it save method is called within the vital service
//
//        // Use any() to prevent subtle nanosecond mismatch failures
//        every { vitalRepository.save(any()) } returns vital1
//
//        // 2. Act
//        val savedVital = vitalService.store(vitalBundle)
//
//        // 3. Assert
//        assertEquals(2, savedVital.size)
//    }
//
//}
