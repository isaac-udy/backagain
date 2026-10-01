package platform.server.http

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.log
import io.ktor.server.http.content.CompressedFileType
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import java.io.File

/**
 * Serves the Compose for Web bundle from [directory]: the output of
 * `:app:client:web:wasmJsBrowserDistribution`.
 *
 * * A `.wasm` request is answered with its pre-compressed `.wasm.br` sibling when the browser
 *   accepts Brotli. The bundle's wasm files are content-hashed, so they are cached for a year.
 * * The entry points keep their names across builds, so the browser revalidates them every load.
 * * A path with no file extension answers with `index.html`, so a deep link like `/s/cloud-run`
 *   loads the app and the app's router takes it from there. A missing file is a 404.
 * * Everything sent counts against [budget], and once it is spent the bundle answers 503.
 */
fun Route.webBundle(directory: File, budget: BandwidthBudget) {
    val index = File(directory, "index.html")
    route("/") {
        install(bandwidthCap(budget))
        staticFiles("/", directory) {
            preCompressed(CompressedFileType.BROTLI)
            contentType { file ->
                if (file.extension == "wasm") ContentType("application", "wasm") else null
            }
            modify { file, call ->
                call.response.header(HttpHeaders.CacheControl, cacheControlFor(file))
            }
            fallback { requestedPath, call ->
                if (File(requestedPath).extension.isEmpty()) {
                    call.response.header(HttpHeaders.CacheControl, cacheControlFor(index))
                    call.respondFile(index)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

private fun bandwidthCap(budget: BandwidthBudget) = createRouteScopedPlugin("BandwidthCap") {
    onCall { call ->
        if (budget.isExhausted()) {
            call.respondText("The deck has used its bandwidth for now.", status = HttpStatusCode.ServiceUnavailable)
        }
    }
    onCallRespond { call, body ->
        val length = (body as? OutgoingContent)?.contentLength ?: return@onCallRespond
        if (budget.spend(length)) {
            call.application.log.warn("The web bundle's bandwidth budget is spent: serving 503 until it resets.")
        }
    }
}

private val entryPoints = setOf("index.html", "App.js", "wasm-manifest.js")

private fun cacheControlFor(file: File): String = when {
    file.name in entryPoints -> "no-cache"
    file.extension == "wasm" -> "public, max-age=31536000, immutable"
    else -> "public, max-age=3600"
}
