package com.orielair.api.vital.valueObject.baseline

data class QuantileSketchState(
    val  algorithm: String = "KLL",
    val  data: ByteArray = ByteArray(0),
)