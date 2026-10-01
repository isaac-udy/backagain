import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import backagain.ArchiveNaming

/**
 * Convention plugin for Kotlin Multiplatform library modules.
 *
 * Applies: KotlinMultiplatform, KotlinSerialization
 * Configures: archive naming, the JVM and WasmJS targets, and compiler options.
 *
 * WasmJS is the shipped client. The JVM target exists so `:api` modules reach the server, and so
 * client logic can be unit-tested on the JVM without a browser.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

private val libs = versionCatalogs.named("libs")

base {
    // Same constraint as backagain.jvm-base: the jvm target's jar reaches the server's runtime
    // classpath, where every `:feature:*:api` would otherwise be `api-jvm.jar`.
    archivesName.set(ArchiveNaming.baseNameFor(project.path))
}

kotlin {
    jvm {
        // Must be the DSL-level pin: a tasks.withType<KotlinJvmCompile> override changes the
        // bytecode but not the target's published metadata, leaving backagain.jvm-base-pinned
        // consumers unable to consume (or inline from) a jvm() variant floated to the ambient JDK.
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
        )
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-serialization").get())
        }
    }
}
