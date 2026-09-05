package com.orielair.api.vital.valueObject.baseline

data class MomentsState(
    var count: Long = 0,
    var mean: Double = 0.0,
    var m2: Double = 0.0,
    var min: Double = 0.0,
    var max: Double = 0.0,
)