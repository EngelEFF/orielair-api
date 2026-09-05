package com.orielair.api.vital.service.baseline

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.jvm.java

@Component
class BaselineAggregatorCheckpointScheduler(
    private val windowProvider: AggregationWindowProvider,
    private val checkpointFlushService: BaselineAggregatorCheckpointFlushService
) {

    private val logger = LoggerFactory.getLogger(BaselineAggregatorCheckpointScheduler::class.java)


    /*
     * Runs at the beginning of every 4-hour boundary:
     *
     * 00:00
     * 04:00
     * 08:00
     * 12:00
     * 16:00
     * 20:00
     *
     * At the boundary, the window that just ended must be flushed.
     */
    @Scheduled(cron = "0 0 */4 * * *", zone = "UTC")
  //  @Scheduled(cron = "0 * * * * *", zone = "UTC")
    fun flushCompletedAggregationWindow() {
        // Determine the 4-hour window that has just completed.
        val completedWindow = windowProvider.currentWindow(Instant.now())
        // Persist the state of every active baseline aggregator
        // for the completed aggregation window.
        checkpointFlushService.flushCheckpoints(
            window = completedWindow
        )

        logger.info("✅✅Baseline Aggregator Checkpoints flush successful✅✅");
    }
}