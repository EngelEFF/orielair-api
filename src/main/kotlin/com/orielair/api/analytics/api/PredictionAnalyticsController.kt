package com.orielair.api.analytics.api

import com.orielair.api.analytics.mapper.impl.PredictionHistoryRecordMapper
import com.orielair.api.analytics.persistence.dto.PredictionHistoryRecordDto
import com.orielair.api.analytics.service.PredictionAnalyticsService
import com.orielair.api.prediction.port.PredictionHistoryRecord
import com.orielair.api.prediction.service.PredictionService
import com.orielair.api.vital.mapper.Mapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID



@RestController
@RequestMapping("/analytics/predictions")
class PredictionAnalyticsController (val predictionAnalyticsService: PredictionAnalyticsService,
    val predictionHistoryRecordMapper: PredictionHistoryRecordMapper
)
{
    @GetMapping()
    fun readPredictionHistory(
        @RequestParam( value ="page", defaultValue = "0") page: Int,
        @RequestParam( value ="page_size", defaultValue = "10") limit: Int,
        @RequestParam userId: UUID,
        @RequestParam from: Instant,
        @RequestParam to: Instant,
    ): ResponseEntity<Page<PredictionHistoryRecordDto>>
    {
        val pageable = PageRequest.of(page, limit)
        val predictionHistoriesPage = predictionAnalyticsService.readPredictionHistory(userId, from, to, pageable)

        val predictionHistoriesDto = predictionHistoriesPage.map{ it ->
            predictionHistoryRecordMapper.mapTo(it)
        }
        return ResponseEntity(predictionHistoriesDto, HttpStatus.OK)
    }

}