package com.isaacudy.backagain

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import feature.deck.client.ui.DeckDestination
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import platform.design.BackAgainTheme
import platform.design.ProvideBackAgainViewport

/**
 * The deck is the whole app, and it keeps the address bar in step with the current slide itself,
 * so the root container needs no web history.
 */
@Composable
fun App() {
    // Start Koin for the composition. The Enro ViewModel factory (in BackAgainNavigation)
    // resolves ViewModels from this Koin scope.
    KoinApplication(configuration = koinConfiguration { modules(clientDependencies) }) {
        // The design system is installed once, here, above navigation — so every destination
        // renders inside it and no screen wraps a theme of its own.
        BackAgainTheme {
            ProvideBackAgainViewport {
                val rootContainer = rememberNavigationContainer(backstack = backstackOf(DeckDestination.asInstance()))
                NavigationDisplay(
                    state = rootContainer,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
