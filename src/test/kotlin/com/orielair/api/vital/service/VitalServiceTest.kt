package com.orielair.api.vital.service

import com.orielair.api.vital.persistence.entity.Vital
import com.orielair.api.vital.persistence.repository.VitalHistoryRepository
import com.orielair.api.vital.persistence.repository.VitalRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import java.time.Instant
import java.util.UUID
import kotlin.test.Test


class VitalServiceTest {

    // 1. Declare the variables at the class level
    private lateinit var vitalRepository: VitalRepository
    private lateinit var vitalHistoryRepository: VitalHistoryRepository
    private lateinit var vitalService: VitalService

    @BeforeEach
    fun setUp() {
        vitalRepository = mockk<VitalRepository>()
        vitalHistoryRepository = mockk<VitalHistoryRepository>()
        vitalService = VitalService(vitalRepository, vitalHistoryRepository)
    }

    @AfterEach
    fun tearDown() {} // needed for singletons

    @Test
    fun `save method should return the persisted vital when it succeeds`(){
        val vital = Vital(id = UUID.fromString("df168f80-51ee-4652-a9f7-652d1b84af43"), "1", "50bpm", "15brpm", "10ms", "Awake", "corr-1", Instant.now(), Instant.now())

        // What the repository should do when it save method is called within the vital service

        // Use any() to prevent subtle nanosecond mismatch failures
        every { vitalRepository.save(any()) } returns vital

        // 2. Act
        val savedVital = vitalService.store(vital)

        // 3. Assert
        assertEquals(vital, savedVital)
    }

}
