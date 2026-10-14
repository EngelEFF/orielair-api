package com.orielair.api.vital.mapper.implementation

import com.orielair.api.vital.mapper.Mapper
import com.orielair.api.vital.persistence.dto.VitalBundleDto
import com.orielair.api.vital.persistence.other.VitalBundle
import org.springframework.stereotype.Component
import com.orielair.api.vital.persistence.dto.Metadata as DtoMetadata
import com.orielair.api.vital.persistence.dto.Observation as DtoObservation
import com.orielair.api.vital.persistence.other.Metadata as DomainMetadata
import com.orielair.api.vital.persistence.other.Observation as DomainObservation

@Component
class VitalBundleMapper: Mapper<VitalBundle, VitalBundleDto> {

    override fun mapTo(e: VitalBundle): VitalBundleDto {

        return VitalBundleDto(
            metadata = DtoMetadata(
                bundleId = e.metadata.bundleId,
                userId = e.metadata.userId,
                windowStart = e.metadata.windowStart,
                windowEnd = e.metadata.windowEnd
            ),
            observations = e.observations.map { obs ->
                DtoObservation(       // using DTO Observation
                    type = obs.type,
                    value = obs.values,    // domain 'values' -> DTO 'value'
                    unit = obs.unit,
                    status = obs.statuses, // domain 'statuses' -> DTO 'status'
                    timestamp = obs.timestamp
                )
            }
        )
    }

    override fun mapFrom(d: VitalBundleDto): VitalBundle {
        return VitalBundle(
            metadata = DomainMetadata(  // using Domain Metadata
                bundleId = d.metadata.bundleId,
                userId = d.metadata.userId,
                windowStart = d.metadata.windowStart,
                windowEnd = d.metadata.windowEnd
            ),
            observations = d.observations.map { obs ->
                DomainObservation(      // using Domain Observation
                    type = obs.type,
                    values = obs.value,    // DTO 'value' -> domain 'values'
                    unit = obs.unit,
                    statuses = obs.status, // DTO 'status' -> domain 'statuses'
                    timestamp = obs.timestamp
                )
            }
        )
    }
}