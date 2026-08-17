package com.orielair.api.analytics.api

import com.orielair.api.analytics.mapper.impl.VitalHistoryRecordMapper
import com.orielair.api.analytics.persistence.dto.VitalHistoryRecordDto
import com.orielair.api.analytics.service.VitalAnalyticsService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID


@RestController
@RequestMapping("/analytics/vitals")
class VitalAnalyticsController(val vitalHistoryRecordMapper: VitalHistoryRecordMapper, val vitalAnalyticsService: VitalAnalyticsService) {

    @GetMapping()
    fun getVitalReport(

        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "page_size", defaultValue = "10") pageSize: Int,
        @RequestParam(value = "from" ) from: Instant,
        @RequestParam(value = "to") to: Instant,
        @RequestParam(value = "userId" ) userId: UUID

    ): ResponseEntity<Page<VitalHistoryRecordDto>>{
        val pageable = PageRequest.of(page, pageSize)
        val vitalHistoriesPage = vitalAnalyticsService.readVitalsHistory(userId, from, to, pageable)
        val vitalHistoriesDto = vitalHistoriesPage.map { vitalHistoryRecordMapper.mapTo(it)}
        return ResponseEntity(vitalHistoriesDto, HttpStatus.OK)
    }

    @GetMapping(params = ["vitalType"]) // Matches ONLY if vitalType is in the URL
    fun getVitalReport(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "page_size", defaultValue = "10") pageSize: Int,
        @RequestParam(value = "userId" ) userId: UUID,
        @RequestParam(value = "vitalType" ) vitalType: String,
        @RequestParam(value = "from" ) from: Instant,
        @RequestParam(value = "to") to: Instant
    ): ResponseEntity<Page<VitalHistoryRecordDto>>{

        val pageable = PageRequest.of(page, pageSize)
        val vitalHistoriesPage = vitalAnalyticsService.readVitalsHistory(userId, from, to, pageable)
        val vitalHistoriesDto = vitalHistoriesPage.map { vitalHistoryRecordMapper.mapTo(it)}
        return ResponseEntity(vitalHistoriesDto, HttpStatus.OK)
    }
}