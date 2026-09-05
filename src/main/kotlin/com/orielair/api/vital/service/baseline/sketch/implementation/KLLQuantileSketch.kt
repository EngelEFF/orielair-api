package com.orielair.api.vital.service.baseline.sketch.implementation

import com.orielair.api.vital.service.baseline.sketch.QuantileSketch
import org.apache.datasketches.kll.KllDoublesSketch
import org.apache.datasketches.memory.Memory



// A new instance with a provided / default k value is created when you're not serializing from rawByte array
class KLLQuantileSketch private constructor(
    // Must be declared here to be accessible to the companion object method
    private val sketch: KllDoublesSketch
): QuantileSketch {

    /*
     The value k = 200 sets the sketch's accuracy level, guaranteeing a strict
     rank-error within +/- 1.35 while keeping the memory footprint under 8 KB
     */
    constructor(): this(KllDoublesSketch.newHeapInstance(200))

    override fun add(value: Double) = sketch.update(value)

    override fun merge(other: QuantileSketch) {
        require(other is KLLQuantileSketch) { "Cannot merge incompatible sketch types" }
        sketch.merge(other.sketch) }

    override fun quantile(probability: Double): Double = sketch.getQuantile(probability)

    override fun serialize(): ByteArray = sketch.toByteArray()

    //This is kotlin's way of implement a static java method. When this method is called, with rawBytes array,
    // it transforms the byteArray into a new instance of kllQuantileSketch populated with the historical data from the rawByte array.
    // In essence this method deserializes the serialized sketch which was persisted into a database to the exact state it was before it was serialized
    companion object {
        fun fromBytes(bytes: ByteArray): KLLQuantileSketch {
            require(bytes.isNotEmpty()) {
                "Serialized KLL sketch cannot be empty"
            }


            // 1. Directly convert your Kotlin ByteArray into a native Java MemorySegment
            // val nativeSegment: MemorySegment = MemorySegment.ofArray(bytes)

            val sketch = KllDoublesSketch.heapify(
                Memory.wrap(bytes)
            )

            return KLLQuantileSketch(sketch)
        }
    }
}