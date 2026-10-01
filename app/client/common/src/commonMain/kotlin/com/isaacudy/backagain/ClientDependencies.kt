package com.isaacudy.backagain

import feature.deck.deckClientDependencies
import feature.live.liveClientDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.client.http.httpClientDependencies
import kotlin.time.Clock

/**
 * The client's whole dependency graph. Each feature's `[name]ClientDependencies` is added here,
 * so `ClientDependenciesTest` verifies the same list `App` installs.
 */
val clientDependencies: List<Module> = listOf(
    module {
        single<CoroutineScope> { MainScope() }
        single<Clock> { Clock.System }
    },
    httpClientDependencies,
    liveClientDependencies,
    deckClientDependencies,
)
