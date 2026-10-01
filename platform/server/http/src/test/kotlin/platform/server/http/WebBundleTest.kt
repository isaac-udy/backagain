package platform.server.http

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

class WebBundleTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val bundle: File by lazy {
        folder.newFolder("web").apply {
            File(this, "index.html").writeText(INDEX)
            File(this, "app.wasm.br").writeBytes(ByteArray(BROTLI_SIZE))
        }
    }

    @Test
    fun `a deep link loads the app`() = serve { client ->
        val response = client.get("/s/cloud-run")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(INDEX, response.bodyAsText())
        assertEquals("no-cache", response.headers[HttpHeaders.CacheControl])
    }

    @Test
    fun `a missing file is not answered with the app`() = serve { client ->
        assertEquals(HttpStatusCode.NotFound, client.get("/old.wasm").status)
    }

    @Test
    fun `wasm is served Brotli-encoded, and only to a client that accepts it`() = serve { client ->
        val brotli = client.get("/app.wasm") { header(HttpHeaders.AcceptEncoding, "gzip, br") }
        assertEquals(HttpStatusCode.OK, brotli.status)
        assertEquals("br", brotli.headers[HttpHeaders.ContentEncoding])

        val plain = client.get("/app.wasm") { header(HttpHeaders.AcceptEncoding, "gzip") }
        assertEquals(HttpStatusCode.NotFound, plain.status)
    }

    @Test
    fun `a spent budget turns visitors away from the bundle but not from the API`() =
        serve(BandwidthBudget(listOf(BandwidthBudget.Limit(bytes = BROTLI_SIZE.toLong(), window = 1.hours)))) { client ->
            assertEquals(HttpStatusCode.OK, client.get("/app.wasm") { header(HttpHeaders.AcceptEncoding, "br") }.status)

            assertEquals(HttpStatusCode.ServiceUnavailable, client.get("/").status)
            assertEquals(HttpStatusCode.OK, client.get("/api/ping").status)
        }

    private fun serve(
        budget: BandwidthBudget = BandwidthBudget(emptyList()),
        block: suspend ApplicationTestBuilder.(HttpClient) -> Unit,
    ) = testApplication {
        routing {
            get("/api/ping") { call.respondText("pong") }
            webBundle(bundle, budget)
        }
        block(createClient { followRedirects = false })
    }

    private companion object {
        const val INDEX = "<!doctype html><title>backagain</title>"
        const val BROTLI_SIZE = 1_000
    }
}
