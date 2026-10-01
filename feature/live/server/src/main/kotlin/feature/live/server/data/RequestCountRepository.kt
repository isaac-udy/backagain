package feature.live.server.data

import feature.live.server.domain.CountRequestsServed
import feature.live.server.domain.RestartRequestCount
import platform.server.http.ServedRequests
import java.util.concurrent.atomic.AtomicLong

/** The process counts every request; this counts from where the live session started. */
internal class RequestCountRepository(
    private val servedRequests: ServedRequests,
) {
    private val countedFrom = AtomicLong(servedRequests.total())

    val countRequestsServed = CountRequestsServed {
        servedRequests.total() - countedFrom.toLong()
    }

    val restartRequestCount = RestartRequestCount {
        countedFrom.set(servedRequests.total())
    }
}
