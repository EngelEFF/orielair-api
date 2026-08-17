package com.orielair.api.analytics.mapper.impl

import com.orielair.api.analytics.mapper.Mapper
import com.orielair.api.analytics.persistence.dto.VitalHistoryRecordDto
import com.orielair.api.vital.port.VitalHistoryRecord
import org.springframework.stereotype.Component


@Component
class VitalHistoryRecordMapper: Mapper<VitalHistoryRecord, VitalHistoryRecordDto> {

    override fun mapTo(e: VitalHistoryRecord): VitalHistoryRecordDto {
        return VitalHistoryRecordDto(
            id = e.id,
            userId = e.userId,
            vitalType = e.vitalType,
            value = e.value,
            unit = e.unit,
            physiologicalState = e.physiologicalState,
            sourceEventID = e.sourceEventID,
            correlationId = e.correlationId,
            ingestedAt = e.ingestedAt,
            updatedAt = e.updatedAt,
        )
    }
}