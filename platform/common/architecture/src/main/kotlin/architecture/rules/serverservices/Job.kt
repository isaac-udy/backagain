package architecture.rules.serverservices

import dev.isaacudy.udytils.architecture.*

import architecture.definitions.isServerModule
import architecture.definitions.resolveTypeToken
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration

@Describe("""
    An entry point the server triggers itself, on a timer: a class that implements
    `platform.server.http.BackgroundJob`. Like [Routes](#routes), it lives in
    `feature.[name].server.services` of `:server` and does its work by calling the feature's
    [`server.domain` interfaces](serverdomain.md#domain-interface).

    The server starts every `BackgroundJob` bound in Koin when it starts.
""")
object Job : Construct<ServerServices>(
    requirements = listOf(
        isClassWhere("is named `[Name]Job`") { it.name.endsWith("Job") },
        predicate("resides in `feature.[name].server.services` itself, not in a sub-package") { it.isInServicesRoot() },
        predicate("is declared in a `:server` module") { it.isServerModule() },
    ),
) {
    @Describe("A Job must be `internal`")
    val internalVisibility by rule {
        constrain { decl, _ ->
            val cls = decl as? KoClassDeclaration ?: return@constrain emptyList()
            if (cls.hasInternalModifier) emptyList() else listOf(Violation(cls, "Job must be `internal`"))
        }
    }

    @Describe("A Job must implement `platform.server.http.BackgroundJob`")
    val implementsBackgroundJob by rule {
        rationale("A `[Name]Job` that does not implement `BackgroundJob` is never started.")
        constrain { decl, _ ->
            val cls = decl as? KoClassDeclaration ?: return@constrain emptyList()
            val implements = cls.parents().any {
                cls.containingFile.resolveTypeToken(it.name) == BACKGROUND_JOB
            }
            if (implements) emptyList() else listOf(Violation(cls, "`${cls.name}` does not implement `$BACKGROUND_JOB`"))
        }
    }

    @Describe("A Job may inject only domain interfaces and platform types; persistence stays behind the domain, as it does for Routes")
    val injectsDomainInterfaces by guidance {
        note("`ServerServices.noDataImports` holds the persistence half of this for every class in the layer.")
    }
}

private const val BACKGROUND_JOB = "platform.server.http.BackgroundJob"
