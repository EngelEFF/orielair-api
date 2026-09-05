package com.orielair.api.vital.service.baseline


// Implements the Welford's algorithms in computing the standard deviation and mean continuously.
class Moments(
    var count: Long = 0,
    var mean: Double = 0.0,
    var m2: Double = 0.0,
    var min: Double = Double.POSITIVE_INFINITY,
    var max: Double = Double.NEGATIVE_INFINITY
) {

    fun add(value: Double){
        count++
        val delta = value - mean
        mean += delta / count
        val delta2 = value - mean
        m2 += delta * delta2
        if (value < min) min = value
        if (value > max) max = value
    }


    // This function merges literally two datasets and derive new metrics by combining the individual metrics of the two datasets.
    fun merge(other: Moments){

        // if the other dataset is empty quit
        if (other.count == 0L) return

        // if the current dataset is empty then metrics of the new automatically becomes the current metrics.
        if (this.count == 0L) {
            this.count = other.count
            this.mean = other.mean
            this.m2 = other.m2
            this.min = other.min
            this.max = other.max
            return
        }

        // else derive new metrics by combining the two datasets.
        val newCount = this.count + other.count
        val delta = other.mean - this.mean
        this.mean += delta * other.count / newCount
        this.m2 += other.m2 + delta * delta * this.count * other.count / newCount
        this.count = newCount
        this.min = minOf(this.min, other.min)
        this.max = maxOf(this.max, other.max)

    }
    // Computed getters which generate variance and std whenever they're called.
    val variance: Double get() = if (count > 1) m2 / (count -1 ) else 0.0
    val standardDeviation: Double get() = kotlin.math.sqrt(variance)
}