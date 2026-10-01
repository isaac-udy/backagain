package architecture.rules.feature

import dev.isaacudy.udytils.architecture.*

import architecture.definitions.featureName
import architecture.definitions.isClientModule
import architecture.definitions.isFeatureModule
import architecture.definitions.isFeatureRootPackage
import architecture.definitions.isServerModule
import com.lemonappdev.konsist.api.declaration.KoPropertyDeclaration
import com.lemonappdev.konsist.api.provider.KoContainingFileProvider

@Describe("""
    The configuration for Dependency Injection (DI) that wires the feature together.

    * **Note:** The naming convention is `[name]ClientDependencies` in `:client` and
      `[name]ServerDependencies` in `:server`. The Construct enforces the `Dependencies` suffix;
      the `Client`/`Server` infix is convention.
    * **Note:** The `:app` modules (application shells) are responsible for collecting the DI
      modules provided by feature modules into the final dependency graph. When a new dependency
      module is added, it must be registered in `:app:client:common` or `:app:server`.
    * **Note:** Every application class is bound by constructor reference — `singleOf`,
      `factoryOf`, `scopedOf`, or `viewModelOf`, whichever lifetime the class needs — so the graph
      supplies every constructor parameter, and a registered class gives no parameter a default
      (`ProjectRules.constructorReferenceBindings`,
      `ProjectRules.injectableConstructorsHaveNoDefaults`). A lambda binds what the graph does not
      construct: a typed configuration object, a third-party client built through its builder, a
      Repository property under its domain interface.
    * **Note:** The `:app` shells keep their module lists in one place (`clientDependencies`,
      `serverDependencies`) so a graph-resolution test can verify every registered constructor
      against the graph without booting the application.
""")
object DependencyModule : Construct<FeatureRules>(
    requirements = listOf(
        predicate("resides in the top-level `feature.[name]` package of a `:client` or `:server` module") { decl ->
            // Exactly the feature root — a `Dependencies` property anywhere deeper would let an
            // undeclared layer package slip past the taxonomy by holding only DI code — and on a
            // side module: DI wiring binds implementations, which `:api` never holds.
            decl.isFeatureModule() && decl.isFeatureRootPackage() &&
                (decl.isClientModule() || decl.isServerModule())
        },
        isProperty,
        hasNameEndingWith("Dependencies"),
    ),
) {
    @Describe("A Dependency Module must only bind/provide dependencies that are both defined and implemented in its own feature")
    val ownFeatureBindingsOnly by rule {
        rationale(
            """
            If feature A binds an implementation of feature B's domain interface, feature B's DI
            graph silently depends on feature A, and removing or refactoring A breaks B's wiring
            at runtime rather than at compile time. Each feature owns its own bindings;
            cross-feature consumption goes through `:api` interfaces only.
            """.trimIndent(),
        )
        constrain { decl, _ ->
            val property = decl as? KoPropertyDeclaration ?: return@constrain emptyList()
            val file = property.containingFile
            val owningFeature = file.featureName()
            file.imports
                .filter { import -> import.name.startsWith("feature.") }
                .filter { import -> import.featureName() != owningFeature }
                .map {
                    Violation(
                        decl,
                        "DI module for feature `$owningFeature` binds a dependency from another feature (imports `${it.name}`)",
                    )
                }
        }
    }

    @Describe("A Dependency Module registers a Routes class by constructor reference, bound to `RouteHandler`: `singleOf(::[Name]Routes) bind RouteHandler::class`")
    val routeHandlerBinding by rule {
        note("The server installs every definition `getAll<RouteHandler>()` returns, so binding the class is all it takes to make its routes live.")
        note("Never use `single<RouteHandler> { [Name]Routes(get()) }`: every such definition shares the `RouteHandler` key, so co-registered handlers override each other and `getAll<RouteHandler>()` returns only one. The test catches this form.")
        constrain { decl, _ ->
            val file = (decl as? KoContainingFileProvider)?.containingFile ?: return@constrain emptyList()
            if (Regex("""(scoped|single|factory)\s*<\s*RouteHandler\s*>""").containsMatchIn(file.text)) {
                listOf(Violation(decl, "DI module binds under the shared `RouteHandler` key — co-registered handlers override each other; use `singleOf(::…Routes) bind RouteHandler::class` instead"))
            } else {
                emptyList()
            }
        }
    }
}
