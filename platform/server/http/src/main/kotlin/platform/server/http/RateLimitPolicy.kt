package platform.server.http

import io.ktor.server.plugins.ratelimit.RateLimitConfig
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration

/**
 * A named token bucket. [Scope.PerClient] keys the bucket on the anonymous client id rather than
 * the caller's IP: a conference audience shares one NAT address, so a per-IP limit would throttle
 * the whole room as one person.
 */
data class RateLimitPolicy(
    val name: RateLimitName,
    val limit: Int,
    val refillPeriod: Duration,
    val scope: Scope,
) {
    enum class Scope { PerClient, Global }
}

fun RateLimitConfig.register(policies: List<RateLimitPolicy>) {
    policies.forEach { policy ->
        register(policy.name) {
            rateLimiter(limit = policy.limit, refillPeriod = policy.refillPeriod)
            requestKey { call ->
                when (policy.scope) {
                    RateLimitPolicy.Scope.PerClient -> call.clientId()
                    RateLimitPolicy.Scope.Global -> Unit
                }
            }
        }
    }
}
