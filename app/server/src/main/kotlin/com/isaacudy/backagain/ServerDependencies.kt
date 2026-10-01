package com.isaacudy.backagain

import dev.isaacudy.udytils.postgres.PostgresConfig
import dev.isaacudy.udytils.postgres.koin.postgresDependencies
import feature.live.liveServerDependencies
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.server.http.ServedRequests
import platform.server.postgres.postgresPlatformDependencies
import kotlin.time.Clock

/**
 * The server's whole dependency graph. Each feature's `[name]ServerDependencies` is added here,
 * so `ServerDependenciesTest` verifies the same list the server installs.
 */
internal fun serverDependencies(postgresConfig: PostgresConfig): List<Module> = listOf(
    module {
        single<Clock> { Clock.System }
        singleOf(::ServedRequests)
    },
    postgresDependencies(postgresConfig),
    postgresPlatformDependencies,
    liveServerDependencies,
)
