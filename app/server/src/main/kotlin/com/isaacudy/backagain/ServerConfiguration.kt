package com.isaacudy.backagain

import dev.isaacudy.udytils.postgres.PostgresConfig
import java.io.File
import java.nio.file.Path

/**
 * Everything the server reads from its environment. The `DEV_DATABASE_*` names must stay
 * identical to `backagain.server.DevDatabaseEnvironment` in build-logic — application code can't
 * import build-logic, so the two are kept in sync by convention.
 */
internal object ServerConfiguration {

    private const val DEV_DATABASE_MODE = "BACKAGAIN_DEV_DB"

    private const val DEV_DATABASE_DIRECTORY = "BACKAGAIN_DEV_DB_DIR"

    private const val DEV_DATABASE_SCENARIO = "BACKAGAIN_DEV_SCENARIO"

    private const val PORT = "PORT"

    private const val WEB_DIRECTORY = "BACKAGAIN_WEB_DIR"

    private const val DEFAULT_PORT = 8080

    val devDatabaseMode: String? get() = System.getenv(DEV_DATABASE_MODE)

    val devDatabaseScenario: String? get() = System.getenv(DEV_DATABASE_SCENARIO)

    val devDatabaseDirectory: Path
        get() = System.getenv(DEV_DATABASE_DIRECTORY)
            ?.takeIf { it.isNotBlank() }
            ?.let { Path.of(it) }
            ?: Path.of("build", "dev-postgres")

    val serverPort: Int get() = System.getenv(PORT)?.toIntOrNull() ?: DEFAULT_PORT

    /** The built web client to serve, or null to serve the API alone. */
    val webDirectory: File?
        get() = System.getenv(WEB_DIRECTORY)
            ?.takeIf { it.isNotBlank() }
            ?.let(::File)
            ?.takeIf { it.isDirectory }

    /**
     * Builds the [PostgresConfig] for a real (non-dev) Postgres. The udytils toolkit ships no
     * app-specific defaults, so the variable names and the defaults below — which target a local
     * Postgres — belong to the application.
     */
    val postgresConfigFromEnv: PostgresConfig
        get() = PostgresConfig(
            jdbcUrl = System.getenv("POSTGRES_URL") ?: "jdbc:postgresql://localhost:5432/backagain",
            username = System.getenv("POSTGRES_USER") ?: "dev",
            password = System.getenv("POSTGRES_PASSWORD") ?: "dev",
            maxPoolSize = System.getenv("POSTGRES_MAX_POOL_SIZE")?.toIntOrNull()
                ?: PostgresConfig.DEFAULT_MAX_POOL_SIZE,
            poolName = "backagain-postgres",
        )
}
