package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.valueObject.baseline.AggregationWindow
import com.orielair.api.vital.valueObject.baseline.BaselineKey
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset



// This is the in memory registry which holds baseline aggregators for each vital state i.e. AWAKE HEART RATE for a specific user
@Service
class BaselineAggregatorRegistry {

    private val aggregators =
        mutableMapOf<BaselineKey, BaselineAggregator>()

    fun getOrCreate(key: BaselineKey): BaselineAggregator {
        return aggregators.getOrPut(key) {
            BaselineAggregator()
        }
    }

    fun get(key: BaselineKey): BaselineAggregator? {
        return aggregators[key]
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