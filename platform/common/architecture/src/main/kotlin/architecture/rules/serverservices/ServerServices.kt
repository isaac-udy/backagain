package architecture.rules.serverservices

import dev.isaacudy.udytils.architecture.*

import architecture.definitions.containingFilePackage
import architecture.definitions.isFeatureModule
import com.lemonappdev.konsist.api.declaration.KoBaseDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration

@Describe("""
    `feature.[name].server.services` defines the contract between client and server, and the
    server's entry points. The contract lives in `:api`, so both the client and server see it; the
    implementation lives in `:server` under the same package name.

    Client and server talk plain HTTP and WebSockets with JSON bodies. An
    [Api contract](#api-contract) names the routes and nests the `@Serializable` request and
    response types; a [Routes](#routes) class installs those paths into Ktor's routing.
    A [Job](#job) is the other kind of entry point: work the server starts on a timer rather than
    in answer to a request. The work an entry point triggers is not declared here — it is stated as
    [`server.domain` interfaces](serverdomain.md#domain-interface) and done by
    [UseCases](serverdomain.md#use-case).

    On the client, [Repositories](clientdata.md#repository) (in `client.data`) call the contract's
    paths with a Ktor `HttpClient`. On the server, a Routes class composes domain interfaces;
    persistence sits behind them, in [`server.data`](serverdata.md), which this layer never imports.

    ## Cross-layer dependencies

    Within a feature, the layer dependency rules are:

    * The `client.domain` and `server.domain` layers import feature roots only.
    * `server.services` may depend on [`server.domain`](serverdomain.md) — and never on
      [`server.data`](serverdata.md) (`ServerServices.noDataImports`).
    * `server.data` may depend on `server.domain` — and never on `server.services`
      (`ServerData.noServiceImports`).
    * `client.data` may depend on `client.domain` and on this layer's contracts, so Repositories
      can call the server (`ClientData.clientServerDependencyRestriction`).
    * `client.ui` may depend on `client.domain` only; server calls go through
      [Repositories](clientdata.md#repository), which provide
      [domain interfaces](clientdomain.md#domain-interface) for the UI to consume.
    * Nothing depends on `client.ui`.

    The client and server meet only at the contract this layer declares in `:api`. Cross-feature
    use of another feature's services goes through `:api` as well: `ServerServices.crossFeatureViaApi`,
    in the [rules](#rules) below.

    An Api contract exists for clients to call, not for server features to compose. One server
    feature reaches another through the capability the owner publishes — a
    [`server.domain` interface](serverdomain.md#domain-interface) whose file resides in `:api` —
    which is the same channel the domain layers use.

    ## Sub-packages

    Any sub-package of the layer is an ordinary subsystem under
    `ProjectRules.subsystemVisibility`: it sees its own package, its direct children, and its
    ancestors up to the layer root — never a sibling.

    ## Persistence

    Routes reach persistence only through
    [`server.domain` interfaces](serverdomain.md#domain-interface) provided by
    [Repositories](serverdata.md#repository); the Postgres conventions and codegen pipeline are
    documented on [`server.data`](serverdata.md).
""")
object ServerServices : RuleGroup(
    inPackage = "feature..server.services..",
    constructs = listOf(
        ApiContract,
        Routes,
        Job,
    ),
) {

    // ---- cross-layer dependency rules (layer-level — not tied to one construct) ---------------
    @Describe("The `server.services` layer must never import `server.data`")
    val noDataImports by rule {
        rationale(
            """
            `server.domain` sits between services and persistence and imports neither: services
            consume domain interfaces, and Repositories provide them. A Routes class that reaches a
            table directly has skipped the layer where the contract
            should have been stated, so nothing else can reuse that access, and nothing names what
            the service actually needed.

            `ServerData.noServiceImports` is the other half. Together they make storage a thing that
            *satisfies* a stated need rather than a thing services reach through.
            """.trimIndent(),
        )
        note("Tested over imports of persistence, wherever the imported file sits: reaching a table is the same act whatever the package holding it is called.")
        scope { scope, exempt ->
            scope.files
                .filter { it.isFeatureModule() && it.isInServerServices() }
                .filterNot { exempt(it) }
                .flatMap { file ->
                    file.imports
                        .filter { it.name.startsWith("feature.") }
                        .filter { it.name.contains(".server.data.") || it.name.contains(".services.storage.") }
                        .map { Violation(file.path, "server.services imports persistence `${it.name}` — state a `server.domain` interface instead") }
                }
        }
    }

    @Describe("The `server.services` layer must not import client code")
    val noClientImports by rule {
        rationale("The client and server meet at the Api contract and nowhere else.")
        scope { scope, exempt ->
            scope.files
                .filter { it.isFeatureModule() && it.isInServerServices() }
                .filterNot { exempt(it) }
                .flatMap { file ->
                    file.imports
                        .filter { it.name.startsWith("feature.") && it.name.contains(".client.") }
                        .map { Violation(file.path, "server.services imports client code `${it.name}`") }
                }
        }
    }

    @Describe("The `services` layer may depend on another feature's `services` only via that feature's `:api` module")
    val crossFeatureViaApi by rule {
        enforcedBy("ModuleRules.clientApiOnly", "ModuleRules.serverApiOnly", "ModuleRules.crossFeatureCodeViaApi")
    }

    @Describe("A `server.services` package imports this layer only through its own package, its direct child subsystems, and its ancestors up to the layer root")
    val subsystemVisibility by rule {
        enforcedBy("ProjectRules.subsystemVisibility")
    }

    @Describe("A `server.services` subsystem package imports `server.domain` only through the matching `server.domain` subsystem package, that package's direct children, and their ancestors")
    val subsystemMirrorsDomain by rule {
        note("A file at the layer root — a Routes class — is unconstrained by the matching-subsystem rule and sees the whole of `server.domain`.")
        enforcedBy("ProjectRules.subsystemMirrorsDomain")
    }
}

/**
 * The services package, `feature.x.server.services.**`. Group 1 is the feature name, group 2 the
 * dotted sub-path after `services` (absent for the services package itself).
 */
internal val servicesPackageRegex = Regex("""^feature\.([^.]+)\.server\.services(?:\.(.+))?$""")

/**
 * The dotted package sub-path after `…server.services` for this declaration, or `null` if it isn't
 * in a services package. `""` for the services package itself; `"tools"` etc. for the
 * sub-packages. Guards against false matches like `…servicesRegistry`.
 */
private fun KoBaseDeclaration.servicesSubpath(): String? =
    servicesPackageRegex.matchEntire(containingFilePackage())?.groupValues?.get(2)

/**
 * In `feature.[name].server.services` itself (the contract, the Routes class) — no further
 * segments. A declaration in a sub-package the architecture does not name classifies as
 * nothing, so the exhaustiveness rules report it.
 */
internal fun KoBaseDeclaration.isInServicesRoot(): Boolean = servicesSubpath() == ""

/** True for a file in `feature.[name].server.services.**` — the file-level form of the group's gate. */
internal fun KoFileDeclaration.isInServerServices(): Boolean {
    val pkg = packagee?.name ?: return false
    return pkg.contains(".server.services")
}
