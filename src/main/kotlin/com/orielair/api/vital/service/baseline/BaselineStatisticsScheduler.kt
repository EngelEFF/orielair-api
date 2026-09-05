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
    @Scheduled(
        cron = "0 0 0 * * MON",
        zone = "UTC"
    )
   // @Scheduled(cron = "0 */2 * * * *", zone = "UTC")
    fun computeWeeklyBaselines() {

        val baselineEnd = Instant.now()
        val baselineStart = baselineEnd.minusSeconds(
            7 * 24 * 60 * 60
        )

        val checkpoints =
            repository.findByWindowStartGreaterThanEqualAndWindowEndLessThanEqual(
                start = Instant.parse("2026-09-05T00:00:00Z"),//baselineStart,
                end = Instant.parse("2026-09-05T04:00:00Z")//baselineEnd
            )

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