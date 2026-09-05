package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.persistence.entity.baseline.BaselineStatistics
import com.orielair.api.vital.persistence.repository.baseline.BaselineStatisticsRepository
import org.springframework.stereotype.Service

@Service
class BaselineStatisticsPersistenceService(
    private val repository: BaselineStatisticsRepository
) {

    fun saveAll(
        baselines: List<BaselineStatistics>
    ): List<BaselineStatistics> {
        return repository.saveAll(baselines)
    }
}