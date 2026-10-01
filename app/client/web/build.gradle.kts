import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("App")
        browser {
            commonWebpackConfig {
                outputFileName = "App.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static(project.projectDir.path)
                }
            }
        }
        binaries.executable()
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(projects.app.client.common)

            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.browser)
        }
    }
}

// macOS Finder/Spotlight drops `.DS_Store` files into the Kotlin/Wasm incremental-compilation
// cache directory; the IC scanner then treats each as a (now-missing) library and crashes with
// "IC internal error: can not find removed library name". Purge them from this module's klib
// cache before any wasm task runs, so incremental dev builds stay reliable on macOS.
tasks.matching { it.name.contains("WasmJs", ignoreCase = true) }.configureEach {
    // A local of this configuration action rather than a script-level `val`: a `doFirst` lambda
    // that captured `layout` or a script property would drag the build script into the task's
    // serialized state, which the configuration cache rejects.
    val klibDir = layout.buildDirectory.dir("klib")
    doFirst {
        val klib = klibDir.get().asFile
        if (klib.exists()) {
            klib.walkTopDown().filter { it.name == ".DS_Store" }.forEach { it.delete() }
        }
    }
}
