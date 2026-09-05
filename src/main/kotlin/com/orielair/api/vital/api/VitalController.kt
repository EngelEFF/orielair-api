package com.orielair.api.vital.api

import com.orielair.api.shared.event.VitalRecorded
import com.orielair.api.vital.mapper.implementation.VitalMapper
import com.orielair.api.vital.persistence.dto.VitalDto
import com.orielair.api.vital.persistence.entity.Vital
import com.orielair.api.vital.service.VitalService
import com.orielair.api.vital.valueObject.baseline.BaselineObservation
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/vitals")
class VitalController (val vitalService: VitalService, val vitalMapper: VitalMapper) {

    @PostMapping
    fun storeVital(@RequestBody dto: VitalDto): /*ResponseEntity<VitalDto>*/ ResponseEntity<VitalRecorded> {
        val vital = vitalMapper.mapFrom(dto)
        val storedVital = vitalService.store(vital)
        val publishedVital = vitalService.publishVital(storedVital)
       // val storedVitalDto = vitalMapper.mapTo(vitalService.store(vital))
        return ResponseEntity(publishedVital, HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun readVital(@PathVariable id: UUID): ResponseEntity<VitalDto> {
        return vitalService.read(id)?.let{ ResponseEntity(vitalMapper.mapTo(it),HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @GetMapping
    fun getVitals(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "limit", defaultValue = "10") pageSize: Int,
    ): ResponseEntity<Page<VitalDto>> {
        val pageable = PageRequest.of(page, pageSize)
        val vitalPage = vitalService.readAll(pageable)
        val vitalPageDto = vitalPage.map{vitalMapper.mapTo(it)}
        return ResponseEntity(vitalPageDto, HttpStatus.OK)
    }

    @PostMapping("/baseline")
    fun storeVital(@RequestBody baselineObservation: BaselineObservation): /*ResponseEntity<VitalDto>*/ ResponseEntity<BaselineObservation> {
        val baselineObservation = vitalService.storeBaselineObservation(baselineObservation)
        return ResponseEntity(baselineObservation, HttpStatus.CREATED)
    }

}