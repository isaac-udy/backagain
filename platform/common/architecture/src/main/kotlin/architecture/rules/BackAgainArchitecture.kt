package architecture.rules

import architecture.definitions.isFeatureModule
import architecture.projectScope
import dev.isaacudy.udytils.architecture.*
import architecture.rules.clientdata.ClientData
import architecture.rules.clientdomain.ClientDomain
import architecture.rules.clientui.ClientUi
import architecture.rules.designsystem.DesignSystemRules
import architecture.rules.feature.FeatureRules
import architecture.rules.module.ModuleRules
import architecture.rules.project.ProjectRules
import architecture.rules.serverdata.ServerData
import architecture.rules.serverdomain.ServerDomain
import architecture.rules.serverservices.ServerServices

/**
 * The project's architecture definition: the rule groups in document order, the scope the rules
 * govern, and the docs layout. The [Describe] text is the README template — `{{toc}}` expands to
 * the generated doc list.
 */
@Describe("""
    # Architecture

    backagain is built from vertical feature slices (`:feature:[name]:{api,client,server}`) over
    shared infrastructure (`:platform`), assembled by thin application shells (`:app`), following
    the [UKPT](https://github.com/isaac-udy/ukpt) template. Module-graph rules keep the slices
    independent.

    A declaration's **package** says what it is; the Gradle **module** it lives in says who may see
    it. A feature is rooted at `feature.[name]`, which holds its shared vocabulary — the domain
    models that both the client and server use. One level down is `client` or `server`; two levels
    down is a layer within that. The deeper the package, the more private the code.

    ```
    client.ui → client.domain ← client.data → [ contract ] ← server.services → server.domain ← server.data
    ```

    The domain layer is the core of the application: it defines the interfaces and models that the
    other layers consume or implement. `client.ui` and `server.services` consume them; `client.data`
    and `server.data` define `Repository` classes that implement the interfaces and produce the
    models. The client and server communicate only through the Api contract, and `client.data` is
    the only client package that may import it.

    The rules govern the feature modules. `build-logic`, test sources, and this rule module itself
    are not tested.

    Rules land enforced from their first commit, never as audits, and no declaration carries an
    `@ArchitectureException`. A rule that cannot be met is a design question, not a setting.
""")
object BackAgainArchitecture : ArchitectureDefinition(
    groups = listOf(
        ModuleRules,
        FeatureRules,
        ClientDomain,
        ClientData,
        ClientUi,
        ServerServices,
        ServerDomain,
        ServerData,
        DesignSystemRules,
        ProjectRules,
    ),
    scope = { projectScope },
    membership = { it.isFeatureModule() },
    docs = DocsConfig(module = "platform/common/architecture"),
)
