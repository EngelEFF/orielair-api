package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.persistence.entity.baseline.BaselineAggregatorCheckpoint
import com.orielair.api.vital.valueObject.baseline.BaselineAggregatorState
import com.orielair.api.vital.valueObject.baseline.MomentsState
import com.orielair.api.vital.valueObject.baseline.QuantileSketchState
import org.springframework.stereotype.Service


@Service
class BaselineAggregatorRestoreService {

    // restore baseline aggregator from a checkpoint (database)
    fun restoreBaselineAggregator(checkpoint: BaselineAggregatorCheckpoint): BaselineAggregator {
        val baselineAggregatorState = BaselineAggregatorState(
            moment = MomentsState(
                m2 = checkpoint.momentM2!!,
                mean = checkpoint.momentMean!!,
                count = checkpoint.momentCount!!,
                max = checkpoint.momentMax!!,
                min = checkpoint.momentMin!!
            ),
            quantile = QuantileSketchState(
                algorithm = checkpoint.quantileAlgorithm!!,
                data = checkpoint.quantileState!!
            )

        )
        val restoredBaselineAggregator = BaselineAggregator.restore(baselineAggregatorState)

        println("Baseline Statistics from the restored baseline aggregator: ${restoredBaselineAggregator.buildStatistics()}")
        return restoredBaselineAggregator
    }



}