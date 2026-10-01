package backagain

import kotlin.test.Test
import kotlin.test.assertEquals

class ArchiveNamingTest {

    @Test
    fun flattensAProjectPathIntoAnArtifactName() {
        assertEquals("app-server", ArchiveNaming.baseNameFor(":app:server"))
        assertEquals("feature-live-api", ArchiveNaming.baseNameFor(":feature:live:api"))
        assertEquals("platform-server-postgres", ArchiveNaming.baseNameFor(":platform:server:postgres"))
    }

    @Test
    fun givesModulesSharingALeafDirectoryDistinctNames() {
        val paths = listOf(
            ":app:server",
            ":feature:live:api",
            ":feature:live:server",
            ":feature:deck:api",
            ":feature:deck:client",
            ":platform:server:postgres",
            ":platform:server:development",
        )

        val names = paths.map(ArchiveNaming::baseNameFor)

        assertEquals(names.size, names.toSet().size, "colliding archive names: $names")
    }

    @Test
    fun fallsBackToARootNameForTheRootProject() {
        assertEquals("root", ArchiveNaming.baseNameFor(":"))
    }
}
