package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.valueObject.baseline.AggregationWindow
import com.orielair.api.vital.valueObject.baseline.BaselineKey
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write


// This is the in memory registry which holds baseline aggregators for each vital state i.e. AWAKE HEART RATE for a specific user
@Component
class BaselineAggregatorRegistry {

    private val lock = ReentrantReadWriteLock()


    /*
     * This map represents the currently active aggregation window.
     * It is replaced atomically during rotation.
     */
    private var aggregators =
        mutableMapOf<BaselineKey, BaselineAggregator>()




    /*
     * Adds a value to the aggregator associated with the key.
     *
     * The entire operation is protected by the read lock so that
     * rotation cannot happen between:
     *
     *     getOrCreate()
     *     add()
     *
     * This guarantees that an observation is assigned completely
     * to either the old window or the new window.
     */
    fun add(
        key: BaselineKey,
        value: Double
    ) {
        lock.read {

            val aggregator = aggregators.getOrPut(key) {
                BaselineAggregator()
            }

            aggregator.add(value)
        }
    }








    /*
     * Retrieves an existing aggregator or creates a new one.
     */
    fun getOrCreate(
        key: BaselineKey
    ): BaselineAggregator =
        lock.read {
            aggregators.getOrPut(key) {
                BaselineAggregator()
            }
        }



    /*
    * Retrieves an existing aggregator.
    */
    fun get(
        key: BaselineKey
    ): BaselineAggregator? =
        lock.read {
            aggregators[key]
        }


    /*
     * Atomically closes the current aggregation window.
     *
     * The existing map is detached and returned.
     *
     * A completely new map becomes active immediately.
     *
     * Therefore:
     *
     *     old aggregators → completed window
     *     new aggregators → next window
     */
    fun rotate(): Map<BaselineKey, BaselineAggregator> =
        lock.write {

            val completedAggregators = aggregators

            aggregators =
                mutableMapOf()

            completedAggregators
        }

    fun remove(key: BaselineKey): BaselineAggregator? {
        return aggregators.remove(key)
    }

    fun contains(key: BaselineKey): Boolean {
        return key in aggregators
    }

    fun size(): Int {
        return aggregators.size
    }

    fun keys(): Set<BaselineKey> {
        return aggregators.keys
    }

}