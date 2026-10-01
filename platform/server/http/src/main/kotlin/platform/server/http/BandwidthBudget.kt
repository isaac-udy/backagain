package platform.server.http

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * How many bytes may go out per window, across every caller.
 *
 * Cloud Run bills outbound data with no ceiling, and the bundle is megabytes: without a cap, anyone
 * with a download loop sets the bill. Exhausting it takes the site down for new visitors, which
 * someone with that loop could do anyway to a single instance.
 */
class BandwidthBudget(
    limits: List<Limit>,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    data class Limit(val bytes: Long, val window: Duration)

    private val windows = limits.map { Window(it, timeSource.markNow()) }

    fun isExhausted(): Boolean = synchronized(this) {
        windows.any { it.roll().isSpent }
    }

    /** @return whether this spend is the one that exhausted a window. */
    fun spend(bytes: Long): Boolean = synchronized(this) {
        windows.fold(false) { exhausted, window ->
            val wasSpent = window.roll().isSpent
            window.spent += bytes
            exhausted || (!wasSpent && window.isSpent)
        }
    }

    private inner class Window(val limit: Limit, var start: TimeMark) {
        var spent = 0L
        val isSpent get() = spent >= limit.bytes

        fun roll(): Window = apply {
            if (start.elapsedNow() >= limit.window) {
                start = timeSource.markNow()
                spent = 0
            }
        }
    }
}
