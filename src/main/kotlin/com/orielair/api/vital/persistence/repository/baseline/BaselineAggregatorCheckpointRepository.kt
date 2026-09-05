package com.orielair.api.vital.persistence.repository.baseline

import com.orielair.api.vital.persistence.entity.baseline.BaselineAggregatorCheckpoint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BaselineAggregatorCheckpointRepository: JpaRepository<BaselineAggregatorCheckpoint, UUID> {
}