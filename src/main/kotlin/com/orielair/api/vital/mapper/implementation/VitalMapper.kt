package com.orielair.api.vital.mapper.implementation

import com.orielair.api.vital.mapper.Mapper
import com.orielair.api.vital.persistence.dto.VitalDto
import com.orielair.api.vital.persistence.entity.Vital
import org.springframework.stereotype.Component

@Component
class VitalMapper: Mapper<Vital, VitalDto> {

    override fun mapTo(e: Vital): VitalDto {
        return VitalDto(
            id = e.id,
            userId = e.userId,
            windowId = e.windowId,

            observationType = e.observationType,
            value = e.value,
            unit = e.unit,
            timestamp = e.timestamp,
            physiologicalState = e.physiologicalState,

            ingestedAt = e.ingestedAt,
        )
    }

    override fun mapFrom(d: VitalDto): Vital {
         return Vital(
             id = d.id,
             userId = d.userId,
             windowId = d.windowId,

             observationType = d.observationType,
             value = d.value,
             unit = d.unit,
             timestamp = d.timestamp,
             physiologicalState = d.physiologicalState,

             ingestedAt = d.ingestedAt,
         )
    }
}