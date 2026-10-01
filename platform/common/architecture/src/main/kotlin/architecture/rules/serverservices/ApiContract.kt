package architecture.rules.serverservices

import dev.isaacudy.udytils.architecture.*

import com.lemonappdev.konsist.api.declaration.KoObjectDeclaration

@Describe("""
    The client-server contract, declared in `:api` so both sides compile against it: an `object`
    named `[Name]Api` that nests each HTTP route as a Ktor `@Resource` class and each request and
    response body as a `@Serializable` class. A WebSocket route, which Ktor's type-safe routing
    does not cover, is a `const val` path. The server installs the routes in a
    [Routes](#routes) class (`post<WallApi.Messages> { … }`); the client calls them from a
    [Repository](clientdata.md#repository) with the same classes
    (`httpClient.post(WallApi.Messages()) { … }`).
""")
object ApiContract : Construct<ServerServices>(
    requirements = listOf(
        isObjectWhere("is an `object`") { true },
        hasNameEndingWith("Api"),
        predicate("resides in `feature.[name].server.services` itself, not in a sub-package") { it.isInServicesRoot() },
    ),
) {
    @Describe("An Api contract must live in `feature.[name].server.services` of the `:api` module")
    val contractLivesInApi by rule {
        constrain { decl, _ ->
            val contract = decl as? KoObjectDeclaration ?: return@constrain emptyList()
            if (contract.containingFile.path.contains("/api/src/")) {
                emptyList()
            } else {
                listOf(Violation(contract, "Api contract is declared outside the `:api` module"))
            }
        }
    }

    @Describe("An Api contract's properties must be `const val` paths starting with `/api/` or `/ws/`")
    val pathsAreConstants by rule {
        rationale(
            """
            A path is the one string both sides must agree on byte for byte. Declared once in the
            shared contract — as a `@Resource` class, or a constant where a route cannot be one — a
            typo is a compile error on whichever side misspells it, and a feature's whole URL
            surface reads in one place.
            """.trimIndent(),
        )
        constrain { decl, _ ->
            val contract = decl as? KoObjectDeclaration ?: return@constrain emptyList()
            contract.properties(includeNested = false)
                .filterNot { property ->
                    val path = property.value?.trim('"').orEmpty()
                    property.hasConstModifier && (path.startsWith("/api/") || path.startsWith("/ws/"))
                }
                .map { Violation(it, "`${it.name}` is not a `const val` path under `/api/` or `/ws/`") }
        }
    }

    @Describe("Every class and object nested in an Api contract must be `@Serializable`")
    val nestedTypesSerializable by rule {
        rationale(
            """
            The nested types are what crosses the wire, encoded on one side and decoded on the
            other by the same generated serializer. A nested helper that is not a wire type belongs
            beside the code that uses it, not in the contract.
            """.trimIndent(),
        )
        constrain { decl, _ ->
            val contract = decl as? KoObjectDeclaration ?: return@constrain emptyList()
            val classes = contract.classes(includeNested = true)
                .filterNot { it.hasAnnotation { annotation -> annotation.name == "Serializable" } }
                .map { Violation(it, "`${it.name}` is nested in an Api contract but is not `@Serializable`") }
            val objects = contract.objects(includeNested = true)
                .filterNot { it.hasAnnotation { annotation -> annotation.name == "Serializable" } }
                .map { Violation(it, "`${it.name}` is nested in an Api contract but is not `@Serializable`") }
            classes + objects
        }
    }

    @Describe("A failed request must answer with a non-2xx status and an `ApiError` body; a response type only models success")
    val errorsViaStatus by guidance {
        note("The server throws `ApiException`, which the StatusPages plugin turns into the status and body; the client's `HttpClient` turns a non-2xx response back into an `ApiException`.")
    }
}
