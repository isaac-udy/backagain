package architecture.rules.serverservices

import dev.isaacudy.udytils.architecture.*

import architecture.definitions.containsPackageSegment
import architecture.definitions.isServerModule
import architecture.definitions.resolveTypeToken
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoParameterDeclaration
import com.lemonappdev.konsist.api.provider.KoFullyQualifiedNameProvider

@Describe("""
    The server side of an [Api contract](#api-contract): a class that implements
    `platform.server.http.RouteHandler` and installs the contract's routes into Ktor's routing.
    It lives in `feature.[name].server.services` of `:server`, the same package as the contract,
    and answers each request by composing the feature's
    [`server.domain` interfaces](serverdomain.md#domain-interface).

    The server finds every `RouteHandler` through Koin, so a new Routes class is live once its
    feature's Dependency Module binds it (`FeatureRules.DependencyModule.routeHandlerBinding`).
""")
object Routes : Construct<ServerServices>(
    requirements = listOf(
        isClassWhere("is named `[Name]Routes`") { it.name.endsWith("Routes") },
        predicate("resides in `feature.[name].server.services` itself, beside the contract, not in a sub-package") { it.isInServicesRoot() },
        // The contract lives in :api so both sides see it; a Routes class declared there would be
        // published server code, so it classifies as nothing and the membership test names it.
        predicate("is declared in a `:server` module") { it.isServerModule() },
    ),
) {
    @Describe("A Routes class must be `internal`")
    val internalVisibility by rule {
        constrain { decl, _ ->
            val cls = decl as? KoClassDeclaration ?: return@constrain emptyList()
            if (cls.hasInternalModifier) emptyList() else listOf(Violation(cls, "Routes class must be `internal`"))
        }
    }

    @Describe("A Routes class must implement `platform.server.http.RouteHandler`")
    val implementsRouteHandler by rule {
        rationale(
            """
            The construct classifies on name and package alone. A `[Name]Routes` that does not
            implement `RouteHandler` is never installed, so its routes answer 404 with nothing to
            say why.
            """.trimIndent(),
        )
        constrain { decl, _ ->
            val cls = decl as? KoClassDeclaration ?: return@constrain emptyList()
            val implements = cls.parents().any {
                cls.containingFile.resolveTypeToken(it.name) == ROUTE_HANDLER
            }
            if (implements) emptyList() else listOf(Violation(cls, "`${cls.name}` does not implement `$ROUTE_HANDLER`"))
        }
    }

    @Describe("A Routes class must not inject persistence: neither a Repository nor a StorageClass")
    val noPersistenceInjection by rule {
        rationale(
            """
            A Routes class answers a request by composing the feature's `server.domain` interfaces;
            persistence sits on the far side of those interfaces, where `ServerServices.noDataImports`
            keeps it. A Repository is the wiring that provides those interfaces, not a thing to hold —
            injecting it, or the StorageClass under it, states the table the handler wants instead of
            the contract it needs, so nothing else can reuse that access and nothing names what the
            request actually required. It is the same rule that keeps ViewModels off client
            Repositories.
            """.trimIndent(),
        )
        note("Tested on the primary constructor: a parameter whose type is named `[Name]Repository`, `[Name]Storage`, or `[Name]Store`, or whose type resolves into a persistence package.")
        note("This is the constructor-shaped half of `ServerServices.noDataImports`, which measures the same coupling over imports.")
        constrain { decl, _ ->
            val cls = decl as? KoClassDeclaration ?: return@constrain emptyList()
            cls.primaryConstructor?.parameters.orEmpty()
                .filter { param -> param.namesPersistence() }
                .map { Violation(cls, "Routes class injects persistence `${it.type.name}` — state a `server.domain` interface and let a Repository provide it") }
        }
    }

    @Describe("A Routes class may inject its feature's `server.domain` interfaces, other features' `server.domain` interfaces published to `:api`, and platform types")
    val mayInjectDomainInterfaces by guidance

    @Describe("A Routes class must not depend on the `ui` package")
    val noUiDependency by rule {
        rationale(
            """
            Routes run on the server and have no Compose runtime: a UI import here would either
            fail to compile in `:server` or mean a UI type is being treated as data. A shape shared
            with the UI belongs in the feature's `:api` module.
            """.trimIndent(),
        )
        constrain { decl, _ ->
            val cls = decl as? KoClassDeclaration ?: return@constrain emptyList()
            if (cls.containingFile.imports.any { it.name.containsPackageSegment("ui") }) {
                listOf(Violation(decl, "Routes class imports the `ui` package"))
            } else {
                emptyList()
            }
        }
    }
}

private const val ROUTE_HANDLER = "platform.server.http.RouteHandler"

/**
 * True when a constructor parameter names persistence: a `[Name]Repository` / `[Name]Storage` /
 * `[Name]Store` type, or a type that resolves into a persistence package.
 */
private fun KoParameterDeclaration.namesPersistence(): Boolean {
    val head = type.name.substringBefore('<').trimEnd('?').substringAfterLast('.')
    if (head.endsWith("Repository") || head.endsWith("Storage") || head.endsWith("Store")) return true
    val source = type.sourceDeclaration as? KoFullyQualifiedNameProvider ?: return false
    val fqn = source.fullyQualifiedName.orEmpty()
    return fqn.contains(".server.data.") || fqn.contains(".services.storage.")
}
