package backagain.server

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

/**
 * Boots the fat jar the way a container would and waits for it to serve a request.
 *
 * Every other check runs against the Gradle runtime classpath, where the jar's packaging decisions
 * do not exist: a manifest the jar truncated is whole, a dependency the jar dropped is present. So
 * this task asserts the jar left the development subgraph out, then proves a jar in that state
 * still migrates a database and answers.
 *
 * The database comes from [developmentClasspath], which carries exactly what the jar excluded.
 * Adding anything more would defeat the point: a second copy of a dependency re-supplies whatever
 * the jar is missing, and ServiceLoader in particular reads every copy it can see.
 */
abstract class SmokeTestFatJarTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val fatJar: RegularFileProperty

    /** The development-only artifacts the fat jar excluded, put back for this boot alone. */
    @get:Classpath
    abstract val developmentClasspath: ConfigurableFileCollection

    @get:Input
    abstract val mainClass: Property<String>

    /** Jar-entry name patterns whose presence means the packaging exclusions stopped working. */
    @get:Input
    abstract val forbiddenJarEntryPatterns: ListProperty<String>

    /** Log fragments that must appear, in any order, before the boot counts as successful. */
    @get:Input
    abstract val expectedLogFragments: ListProperty<String>

    /** Environment for every boot, on top of the port. */
    @get:Input
    abstract val environment: MapProperty<String, String>

    /** Environment for the boot with [developmentClasspath] alone: what selects its database. */
    @get:Input
    abstract val developmentEnvironment: MapProperty<String, String>

    /** The variable the application reads its listening port from. */
    @get:Input
    abstract val portVariable: Property<String>

    /** The variable the application reads a real database's JDBC URL from. */
    @get:Input
    abstract val databaseUrlVariable: Property<String>

    @get:Input
    abstract val bootTimeoutSeconds: Property<Long>

    /**
     * Not an input: which JVM ran the build says nothing about whether the jar is correctly
     * packaged, and making it one would rerun the task on every toolchain change.
     */
    @get:Internal
    abstract val javaHome: Property<String>

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun smokeTest() {
        val jar = fatJar.get().asFile
        verifyNoForbiddenEntries(jar)
        verifyStartsWithoutDevelopmentClasspath(jar)

        val port = reserveFreePort()
        val classpath = (listOf(jar) + developmentClasspath.files).joinToString(File.pathSeparator) { it.absolutePath }
        logger.lifecycle("Booting ${jar.name} on port $port")
        // `-cp`, not `-jar`: `-jar` ignores the classpath, and the development artifacts are by
        // design not inside the jar.
        val process = launch(
            arguments = listOf("-cp", classpath, mainClass.get()),
            environment = environment.get() + developmentEnvironment.get() + (portVariable.get() to port.toString()),
        )
        val output = StringBuilder()
        val drain = drainOutput(process, output)

        try {
            awaitHttpReady(process, port)
            awaitLogFragments(output)
        } catch (failure: GradleException) {
            report.get().asFile.apply { parentFile.mkdirs() }.writeText(output.snapshot())
            throw GradleException("${failure.message}\n\nServer output:\n${output.snapshot()}", failure)
        } finally {
            process.destroy()
            if (!process.waitFor(30, TimeUnit.SECONDS)) process.destroyForcibly()
            drain.join(TimeUnit.SECONDS.toMillis(5))
        }

        report.get().asFile.apply {
            parentFile.mkdirs()
            writeText(
                buildString {
                    appendLine("${jar.name} alone reached for a database before failing without one")
                    appendLine("${jar.name} booted on port $port with ${environment.get() + developmentEnvironment.get()}")
                    appendLine("no jar entry matching ${forbiddenJarEntryPatterns.get()}")
                    appendLine("saw ${expectedLogFragments.get()}")
                    appendLine()
                    append(output.snapshot())
                },
            )
        }
    }

    private fun verifyNoForbiddenEntries(jar: File) {
        val forbidden = forbiddenJarEntryPatterns.get().map(::Regex)
        val offenders = ZipFile(jar).use { zip ->
            zip.entries().asSequence()
                .map { it.name }
                .filter { name -> forbidden.any { it.matches(name) } }
                .take(FORBIDDEN_ENTRY_SAMPLE)
                .toList()
        }
        if (offenders.isNotEmpty()) {
            throw GradleException(
                "${jar.name} contains development-only entries that the packaging exclusions are " +
                    "supposed to keep out:\n" + offenders.joinToString("\n") { "  $it" },
            )
        }
    }

    /**
     * Boots the jar alone, as the container does, pointed at a database that isn't there. It should
     * get as far as connecting and fail on that. A jar whose production path still reaches into
     * the development subgraph fails before then, on a missing class — which the full boot below
     * can't see, because it puts that subgraph back.
     */
    private fun verifyStartsWithoutDevelopmentClasspath(jar: File) {
        val databaseAddress = "127.0.0.1:${reserveFreePort()}"
        val process = launch(
            arguments = listOf("-jar", jar.absolutePath),
            environment = environment.get() + mapOf(
                portVariable.get() to reserveFreePort().toString(),
                databaseUrlVariable.get() to "jdbc:postgresql://$databaseAddress/unreachable",
            ),
        )
        val output = StringBuilder()
        val drain = drainOutput(process, output)
        if (!process.waitFor(bootTimeoutSeconds.get(), TimeUnit.SECONDS)) process.destroyForcibly()
        drain.join(TimeUnit.SECONDS.toMillis(5))

        val text = output.snapshot()
        val missingClass = MISSING_CLASS_ERRORS.firstOrNull(text::contains)
        if (missingClass != null || databaseAddress !in text) {
            throw GradleException(
                "Booted without the development classpath, ${jar.name} " +
                    (missingClass?.let { "failed on a $it" } ?: "never tried to reach its database at $databaseAddress") +
                    ". A deployed server would not start.\n\nServer output:\n$text",
            )
        }
    }

    private fun launch(arguments: List<String>, environment: Map<String, String>): Process =
        ProcessBuilder(listOf(File(javaHome.get(), "bin/java").absolutePath) + arguments)
            .redirectErrorStream(true)
            .also { it.environment().putAll(environment) }
            .start()

    private fun drainOutput(process: Process, output: StringBuilder): Thread =
        Thread({
            try {
                process.inputStream.bufferedReader().forEachLine { line ->
                    synchronized(output) { output.appendLine(line) }
                }
            } catch (_: IOException) {
                // On Linux, destroying the process closes this stream mid-read.
            }
        }, "smoke-test-output").apply {
            isDaemon = true
            start()
        }

    private fun awaitHttpReady(process: Process, port: Int) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(bootTimeoutSeconds.get())
        while (System.nanoTime() < deadline) {
            if (!process.isAlive) {
                throw GradleException("The server exited with code ${process.exitValue()} before it served a request.")
            }
            if (respondsOk(port)) return
            Thread.sleep(POLL_INTERVAL_MILLIS)
        }
        throw GradleException(
            "The server did not answer on http://127.0.0.1:$port$HEALTH_PATH within ${bootTimeoutSeconds.get()}s.",
        )
    }

    private fun respondsOk(port: Int): Boolean = try {
        val connection = URI("http://127.0.0.1:$port$HEALTH_PATH").toURL().openConnection() as HttpURLConnection
        connection.connectTimeout = POLL_TIMEOUT_MILLIS
        connection.readTimeout = POLL_TIMEOUT_MILLIS
        try {
            connection.responseCode == HttpURLConnection.HTTP_OK
        } finally {
            connection.disconnect()
        }
    } catch (_: Exception) {
        // Nothing is listening yet, which is the normal state for most of the boot.
        false
    }

    /**
     * Both fragments are logged before the HTTP endpoint opens, so they are normally already
     * present; the grace period only covers the appender being a step behind the socket.
     */
    private fun awaitLogFragments(output: StringBuilder) {
        val expected = expectedLogFragments.get()
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(LOG_GRACE_SECONDS)
        while (System.nanoTime() < deadline) {
            val text = output.snapshot()
            val missing = expected.filterNot(text::contains)
            if (missing.isEmpty()) return
            Thread.sleep(POLL_INTERVAL_MILLIS)
        }
        val missing = expected.filterNot(output.snapshot()::contains)
        throw GradleException(
            "The server answered, but its log never mentioned: ${missing.joinToString()}. That is what " +
                "a fat jar with a clobbered ServiceLoader manifest looks like — Flyway reports success " +
                "having found nothing to do.",
        )
    }

    private fun StringBuilder.snapshot(): String = synchronized(this) { toString() }

    /** A fixed port on a developer machine is usually already serving their dev server. */
    private fun reserveFreePort(): Int = ServerSocket(0).use { it.localPort }

    private companion object {
        const val HEALTH_PATH = "/healthz"
        const val POLL_INTERVAL_MILLIS = 500L
        const val POLL_TIMEOUT_MILLIS = 2_000
        const val LOG_GRACE_SECONDS = 10L
        const val FORBIDDEN_ENTRY_SAMPLE = 20
        val MISSING_CLASS_ERRORS = listOf("NoClassDefFoundError", "ClassNotFoundException")
    }
}
