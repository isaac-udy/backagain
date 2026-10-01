package platform.server.http

import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.request.path
import java.util.concurrent.atomic.AtomicLong

/** How many requests this process has answered, not counting Cloud Run's health checks. */
class ServedRequests {
    private val total = AtomicLong()

    fun total(): Long = total.get()

    internal fun record() {
        total.incrementAndGet()
    }
}

fun Application.countServedRequests(servedRequests: ServedRequests) {
    install(
        createApplicationPlugin("ServedRequests") {
            onCall { call ->
                if (call.request.path() != "/healthz") servedRequests.record()
            }
        },
    )
}
