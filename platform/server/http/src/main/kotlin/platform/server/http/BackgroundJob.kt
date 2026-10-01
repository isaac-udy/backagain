package platform.server.http

import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * Work the server does on a timer rather than in answer to a request. The server starts every
 * `BackgroundJob` bound in Koin when it starts, and stops them with it.
 */
interface BackgroundJob {
    val period: Duration

    suspend fun run()
}

fun Application.start(jobs: List<BackgroundJob>) {
    jobs.forEach { job ->
        launch {
            while (isActive) {
                delay(job.period)
                try {
                    job.run()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (failure: Throwable) {
                    log.error("${job::class.simpleName} failed", failure)
                }
            }
        }
    }
}
