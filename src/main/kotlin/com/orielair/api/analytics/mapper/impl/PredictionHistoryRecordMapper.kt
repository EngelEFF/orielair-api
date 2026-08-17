package com.orielair.api.analytics.mapper.impl

import com.orielair.api.analytics.mapper.Mapper
import com.orielair.api.analytics.persistence.dto.PredictionHistoryRecordDto
import com.orielair.api.analytics.persistence.dto.VitalHistoryRecordDto
import com.orielair.api.prediction.port.PredictionHistoryRecord
import com.orielair.api.vital.port.VitalHistoryRecord
import org.springframework.stereotype.Component


@Component
class PredictionHistoryRecordMapper: Mapper<PredictionHistoryRecord, PredictionHistoryRecordDto> {

    override fun mapTo(e: PredictionHistoryRecord): PredictionHistoryRecordDto {
        return PredictionHistoryRecordDto(
          id = e.id,
            userId = e.userId,
            modelVersion = e.modelVersion,
            riskScore = e.riskScore,
            sourceEventID = e.sourceEventId,
            correlationId = e.correlationId,
            createdAt = e.createdAt,
        )
    }
}