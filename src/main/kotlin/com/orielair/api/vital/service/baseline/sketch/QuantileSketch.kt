package com.orielair.api.vital.service.baseline.sketch

// Defines the contract for a quantile Sketch. This will be used to compute percentiles. Implementations can use KLL or other algorithms
// Thus an interface is necessary
interface QuantileSketch {
    fun add(value: Double)
    fun merge(other: QuantileSketch)
    fun quantile(probability: Double): Double
    fun serialize(): ByteArray
}