package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.service.baseline.sketch.implementation.KLLQuantileSketch
import com.orielair.api.vital.valueObject.baseline.BaselineAggregatorState
import com.orielair.api.vital.valueObject.baseline.MomentsState
import com.orielair.api.vital.valueObject.baseline.QuantileSketchState
import com.orielair.api.vital.valueObject.baseline.BaselineStatistics
import org.springframework.stereotype.Service


// This class serves as the single entry point to the quantile sketch and moments baseline aggregators.
// It creates a single baseline aggregator which holds percentiles from QuantileSketch and S.D, Mean, Min & Max from Moments

@Service
class BaselineAggregator private constructor(
    private val moments: Moments ,
    private val quantiles: KLLQuantileSketch
) {

    constructor(): this(
        moments = Moments(),
        quantiles = KLLQuantileSketch()
    )

    fun add(value: Double) {
        moments.add(value)
        quantiles.add(value)
    }

    fun merge(other: BaselineAggregator){
        moments.merge(other.moments)
        quantiles.merge(other.quantiles)
    }

    fun snapshotBaselineAggregatorState(): BaselineAggregatorState {
        // ensures we have data samples before persistence
        require(moments.count > 0) {
            "Cannot snapshotBaselineAggregatorState an empty baseline aggregator"
        }

        return BaselineAggregatorState(
            moment = MomentsState(
                count = moments.count,
                mean = moments.mean,
                m2 = moments.m2,
                min = moments.min,
                max = moments.max
            ),
            quantile = QuantileSketchState(
                algorithm = "KLL",
                data = quantiles.serialize()
            )
        )

    }

    fun buildStatistics(): BaselineStatistics {
        val p25 = quantiles.quantile(0.25)
        val p75 = quantiles.quantile(0.75)

        return BaselineStatistics(
            mean = moments.mean,
            stdDev = moments.standardDeviation,
            min = moments.min,
            max = moments.max,
            median = quantiles.quantile(0.50),
            p01 = quantiles.quantile(0.01),
            p05 = quantiles.quantile(0.05),
            p25 = quantiles.quantile(0.25),
            p75 = quantiles.quantile(0.75),
            p95 = quantiles.quantile(0.95),
            p99 = quantiles.quantile(0.99),
            iqr = p75 - p25,
            madProxy = 0.6745 * (p75 - p25), // Single-pass proxy. Essential for robust measures computation i.e. z-scores
            samples = moments.count,
        )
    }


    // This is used to reconstruct an aggregator using moments and kll quantile sketches from the database
    companion object {

        fun restore(
            state: BaselineAggregatorState
        ): BaselineAggregator {

            // verifies the quantile algorithm. We didn't check for moment because the quantile byteArray conversion verifies the moment as well
            require(state.quantile.algorithm == "KLL") {
                "Unsupported quantile algorithm: ${state.quantile.algorithm}"
            }

            val restoredMoments = Moments(
                count = state.moment.count,
                mean = state.moment.mean,
                m2 = state.moment.m2,
                min = state.moment.min,
                max = state.moment.max
            )

            val restoredQuantiles =
                KLLQuantileSketch.fromBytes(
                    state.quantile.data
                )

            return BaselineAggregator(
                moments = restoredMoments,
                quantiles = restoredQuantiles
            )
        }
    }
}

