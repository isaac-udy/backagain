/**
 * Convention plugin for KMP modules that also use Compose Multiplatform.
 *
 * Applies: backagain.kmp-library, ComposeMultiplatform, ComposeCompiler
 */
plugins {
    id("backagain.kmp-library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Compose 1.12 fails this check for every wasmJs test compilation that reaches Skiko, which every
// Compose library's does. The constraint it guards is real only for runComposeUiTest (CMP-4906):
// Skiko's wasm runtime is loaded by the webpack bundle an executable produces. Plain kotlin.test
// suites run in the browser without one, and a library declaring `binaries.executable()` would only
// add a bundle nothing serves. A module that adds runComposeUiTest on wasmJs must declare
// `wasmJs { binaries.executable() }` and re-enable the task.
tasks.named { it.startsWith("checkComposeUiTestConfigurationFor") }.configureEach {
    enabled = false
}
