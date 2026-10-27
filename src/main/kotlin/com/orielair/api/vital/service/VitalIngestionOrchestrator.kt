package com.orielair.api.vital.service

import com.orielair.api.vital.`enum`.AcceptanceClassification
import com.orielair.api.vital.persistence.other.Observation
import com.orielair.api.vital.persistence.other.VitalBundle

// Add imports for Observation and VitalBundle from their existing packages.

data class RefinedObservation(
    val type: String,
    val value: Int,
    val unit: String,
    val status: Int,
    val timestamp: Long?
)

data class ObservationRejection(
    val observation: RefinedObservation,
    val classification: AcceptanceClassification,
    val reason: String
)

data class MalformedObservationRejection(
    val observation: Observation,
    val reason: String
)

data class UnsupportedObservationRejection(
    val observation: Observation,
    val reason: String
)

data class ObservationFilterResult(
    val accepted: List<RefinedObservation>,
    val rejected: List<ObservationRejection>,
    val malformed: List<MalformedObservationRejection>,
    val unsupported: List<UnsupportedObservationRejection>
)

class VitalIngestionOrchestrator {

    companion object {

        private val observationAcceptancePolicy:
                Map<String, Map<Int, AcceptanceClassification>> = mapOf(

            "PPG_GREEN" to mapOf(
                0 to AcceptanceClassification.GOOD,
                -1 to AcceptanceClassification.BAD
            ),

            "HEART_RATE" to mapOf(
                1 to AcceptanceClassification.GOOD,
                0 to AcceptanceClassification.BAD,
                -2 to AcceptanceClassification.BAD,
                -3 to AcceptanceClassification.BAD,
                -8 to AcceptanceClassification.BAD,
                -10 to AcceptanceClassification.BAD,
                -999 to AcceptanceClassification.BAD
            ),

            "IBI" to mapOf(
                0 to AcceptanceClassification.GOOD,
                -1 to AcceptanceClassification.BAD
            )
        )
    }

    private fun classifyStatus(
        observationType: String,
        status: Int
    ): AcceptanceClassification {
        return observationAcceptancePolicy[observationType]?.get(status)
            ?: AcceptanceClassification.UNSPECIFIED
    }

    fun filterObservations(
        vitalBundle: VitalBundle
    ): ObservationFilterResult {

        val acceptedObservations = mutableListOf<RefinedObservation>()
        val rejectedObservations = mutableListOf<ObservationRejection>()
        val malformedObservations = mutableListOf<MalformedObservationRejection>()
        val unsupportedObservations = mutableListOf<UnsupportedObservationRejection>()

        vitalBundle.observations.forEach { observation ->

            when (observation.type) {

                "HEART_RATE", "PPG_GREEN" -> {

                    val value = observation.values.singleOrNull()
                    val status = observation.statuses.singleOrNull()

                    if (value == null || status == null) {
                        malformedObservations.add(
                            MalformedObservationRejection(
                                observation = observation,
                                reason = "Expected exactly one value and status for " +
                                        "${observation.type}; got " +
                                        "values=${observation.values.size}, " +
                                        "statuses=${observation.statuses.size}."
                            )
                        )
                        return@forEach
                    }

                    val refinedObservation = RefinedObservation(
                        type = observation.type,
                        value = value,
                        unit = observation.unit,
                        status = status,
                        timestamp = observation.timestamp
                    )

                    when (classifyStatus(observation.type, status)) {

                        AcceptanceClassification.GOOD -> {
                            acceptedObservations.add(refinedObservation)
                        }

                        AcceptanceClassification.BAD -> {
                            rejectedObservations.add(
                                ObservationRejection(
                                    observation = refinedObservation,
                                    classification = AcceptanceClassification.BAD,
                                    reason = "${observation.type} status $status " +
                                            "is classified as BAD."
                                )
                            )
                        }

                        AcceptanceClassification.UNSPECIFIED -> {
                            rejectedObservations.add(
                                ObservationRejection(
                                    observation = refinedObservation,
                                    classification = AcceptanceClassification.UNSPECIFIED,
                                    reason = "Unrecognized ${observation.type} " +
                                            "status: $status."
                                )
                            )
                        }
                    }
                }

                "IBI" -> {

                    if (observation.values.isEmpty() && observation.statuses.isEmpty()) {
                        malformedObservations.add(
                            MalformedObservationRejection(
                                observation = observation,
                                reason = "IBI observation contains no value/status pairs: " +
                                        "values=0, statuses=0."
                            )
                        )
                        return@forEach
                    }

                    if (observation.values.size != observation.statuses.size) {
                        malformedObservations.add(
                            MalformedObservationRejection(
                                observation = observation,
                                reason = "IBI value and status lists have different lengths: " +
                                        "values=${observation.values.size}, " +
                                        "statuses=${observation.statuses.size}."
                            )
                        )
                        return@forEach
                    }

                    observation.values.zip(observation.statuses)
                        .forEach { (value, status) ->

                            val refinedObservation = RefinedObservation(
                                type = observation.type,
                                value = value,
                                unit = observation.unit,
                                status = status,
                                timestamp = observation.timestamp
                            )

                            val classification =
                                classifyStatus(observation.type, status)

                            val rejectionReasons = buildList {
                                when (classification) {

                                    AcceptanceClassification.GOOD -> Unit

                                    AcceptanceClassification.BAD ->
                                        add("IBI status $status is classified as BAD.")

                                    AcceptanceClassification.UNSPECIFIED ->
                                        add("Unrecognized IBI status: $status.")
                                }

                                if (value == 0) {
                                    add("IBI interval value is zero.")
                                }
                            }

                            if (rejectionReasons.isEmpty()) {
                                acceptedObservations.add(refinedObservation)
                            } else {
                                rejectedObservations.add(
                                    ObservationRejection(
                                        observation = refinedObservation,
                                        classification = classification,
                                        reason = rejectionReasons.joinToString(" ")
                                    )
                                )
                            }
                        }
                }

                else -> {
                    unsupportedObservations.add(
                        UnsupportedObservationRejection(
                            observation = observation,
                            reason = "Unsupported observation type: " +
                                    "${observation.type}."
                        )
                    )
                }
            }
        }

        return ObservationFilterResult(
            accepted = acceptedObservations,
            rejected = rejectedObservations,
            malformed = malformedObservations,
            unsupported = unsupportedObservations
        )
    }
}