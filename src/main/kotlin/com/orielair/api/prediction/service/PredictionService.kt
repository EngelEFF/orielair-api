package com.orielair.api.prediction.service

import com.orielair.api.prediction.persistence.entity.PredictionHistory
import com.orielair.api.prediction.persistence.repository.PredictionHistoryRepository
import com.orielair.api.prediction.port.PredictionHistoryRecord
import org.springframework.stereotype.Service



@Service
class PredictionService(val predictionHistoryRepository: PredictionHistoryRepository,) {

    fun savePredictionHistory(predictionHistory: PredictionHistory): PredictionHistory {
        return predictionHistoryRepository.save(predictionHistory)
    }

}