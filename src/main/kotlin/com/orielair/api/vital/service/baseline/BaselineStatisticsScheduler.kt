package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.persistence.repository.baseline.BaselineAggregatorCheckpointRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class BaselineStatisticsScheduler(
    private val repository: BaselineAggregatorCheckpointRepository,
    private val computationService: BaselineComputationService,
    private val persistenceService: BaselineStatisticsPersistenceService
) {

    private val logger = LoggerFactory.getLogger(BaselineStatisticsScheduler::class.java)



    /*
     * Runs once every 7 days.
     *
     * The exact day/time is a policy decision.
     * This example runs every Monday at 00:00 UTC.
     */
   /* @Scheduled(
        cron = "0 0 0 * * MON",
        zone = "UTC"
    )*/
   @Scheduled(cron = "0 */15 * * * *", zone = "UTC")
    fun computeWeeklyBaselines() {

        val baselineEnd = Instant.now()
//        val baselineStart = baselineEnd.minusSeconds(
//            7 * 24 * 60 * 60
//        )
        val baselineStart = baselineEnd.minusSeconds( // I'm using this to test the logic resort to the old one of 7 days. You know this must match your defined aggregation window in the AggregationWindowProvider right 😂
            60 * 60
        )

        val checkpoints =
            repository.findByWindowStartGreaterThanEqualAndWindowEndLessThanEqual(
                start =  baselineStart,//baselineStart,
                end = baselineEnd//baselineEnd
            )
//            repository.findByWindowStartGreaterThanEqualAndWindowEndLessThanEqual(
//                start = Instant.parse("2026-11-20T08:00:00Z"),
//                end = Instant.parse("2026-11-20T12:00:00Z")
//            )

        /*
             * Merge checkpoint states and compute one baseline
             * for each unique:
             *
             * user + observation type + physiological state
             */
        val baselines =
            computationService.computeBaselineStatistics(
                checkpoints
            )

        /*
         * Persist the newly computed baseline statistics.
         */
        persistenceService.saveAll(baselines)

        logger.info("✅✅Baseline Statistics Checkpoints: $checkpoints ✅✅");
    }
}