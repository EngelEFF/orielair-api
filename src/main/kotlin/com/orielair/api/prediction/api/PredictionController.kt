package com.orielair.api.prediction.api

import com.orielair.api.prediction.persistence.entity.PredictionHistory
import com.orielair.api.prediction.persistence.repository.PredictionHistoryRepository
import com.orielair.api.prediction.service.PredictionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/predictions")
class PredictionController (
    val predictionService: PredictionService
){

}