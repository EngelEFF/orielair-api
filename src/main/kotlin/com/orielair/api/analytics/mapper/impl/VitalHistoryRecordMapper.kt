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
            vitalId = e.vitalId,
            userId = e.userId,
            windowId = e.windowId,

            value = e.value,
            observationType = e.observationType,
            unit = e.unit,
            timestamp = e.timestamp,
            physiologicalState = e.physiologicalState,

            ingestedAt = e.ingestedAt,

        )
    }
}