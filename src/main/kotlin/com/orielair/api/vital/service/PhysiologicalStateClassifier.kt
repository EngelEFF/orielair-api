package com.orielair.api.vital.service
import com.orielair.api.vital.enum.baseline.PhysiologicalState
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class PhysiologicalStateClassifier {

    @Suppress("UNUSED_PARAMETER")
    fun classify(
        organizedBatch: OrganizedObservationBatch
    ): PhysiologicalState {

        return if (Random.Default.nextBoolean()) {
            PhysiologicalState.AWAKE
        } else {
            PhysiologicalState.ASLEEP
        }
    }
}