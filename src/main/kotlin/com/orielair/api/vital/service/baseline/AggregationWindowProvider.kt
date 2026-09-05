package com.orielair.api.vital.service.baseline

import com.orielair.api.vital.valueObject.baseline.AggregationWindow
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset

// The goals of this class is to compute the current window start and window end using the hours which must be within a single window.

@Service
class AggregationWindowProvider(
    private val windowHours: Long = 4
) {

    init {
        // Ensures the given window is above 0
        require(windowHours > 0) {
            "windowHours must be greater than zero"
        }

        // Ensures we have an even window for accurate breakdowns
        require(24 % windowHours == 0L) {
            "windowHours must divide evenly into 24 hours"
        }
    }

    fun currentWindow(now: Instant): AggregationWindow {
        val utcTime = now.atZone(ZoneOffset.UTC)

        val currentHour = utcTime.hour

        // Find the beginning of the current fixed-size window.
        // For a 4-hour window:
        // 00-03 -> 00:00
        // 04-07 -> 04:00
        // 08-11 -> 08:00
        // 12-15 -> 12:00
        // 16-19 -> 16:00
        // 20-23 -> 20:00

        // In order to achieve this, we have to round down when the hour is below the given hour spacing. Such that the division will round down to the nearest
        // whole number.
        val windowStartHour = (currentHour / windowHours) * windowHours

        val windowStart = utcTime
            .withHour(windowStartHour.toInt()) // This rounds the hour down to the nearest whole number
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .toInstant()

        val windowEnd =
            windowStart.plusSeconds(windowHours * 60 * 60)

        return AggregationWindow(
            start = windowStart,
            end = windowEnd
        )
    }
}