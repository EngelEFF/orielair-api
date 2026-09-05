package com.orielair.api.vital.valueObject.baseline

// Represents the current state of a baseline aggregator in memory before it's persisted into a database.
data class BaselineAggregatorState(val moment: MomentsState, val quantile: QuantileSketchState)