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
            heartRate = e.heartRate,
            respiratoryRate = e.respiratoryRate,
            heartRateVariability = e.heartRateVariability,
            physiologicalState = e.physiologicalState,
            correlationId = e.correlationId,
            ingestedAt = e.ingestedAt,
            updatedAt = e.updatedAt,
        )
    }

    override fun mapFrom(d: VitalDto): Vital {
         return Vital(
             id = d.id,
             userId = d.userId,
             heartRate = d.heartRate,
             respiratoryRate = d.respiratoryRate,
             heartRateVariability = d.heartRateVariability,
             physiologicalState = d.physiologicalState,
             correlationId = d.correlationId,
             ingestedAt = d.ingestedAt,
             updatedAt = d.updatedAt,
         )
    }
}