package architecture.rules.clientui

import dev.isaacudy.udytils.architecture.*

import architecture.definitions.isFeatureModule

@Describe("""
    A `@Composable` function defined in the `..ui..` package that is not a [Screen](#screen).
    Typically a sub-component used by one or more screens, an inline editor, a feature-specific
    overlay, or a `@Preview` function.

    * **Note:** `[Name]ScreenContent` companions (see `ClientUi.Screen.screenContentCompanion`)
      are non-Screen composables, which is why the preview rules live on this Construct.
      For reusable design-system primitives (buttons, fields), prefer a shared composable in
      `:platform:client:design`. Feature-local composables live alongside the Screen they support.

    ### Previews

    * **Note:** Use the unified `@Preview` (`androidx.compose.ui.tooling.preview.Preview`)
      directly in common code, with `compose-ui-tooling-preview` as a `commonMain` dependency.
    * **Note:** Add a `@Preview` per meaningful state as a screen grows. For async screens the meaningful states are: Loading, Error, populated Success, and legitimately-empty Success.
    * **Note:** A screen's `@Preview`(s) live in the same file as the `[Name]ScreenContent` they
      render, next to the Screen — not gathered into a shared "screen previews" file.
    * **Note:** Wrap a screen preview's content in the design module's `BackAgainPreviewFrame`,
      which sizes the render to the project's primary viewport and pins the palette.
""")
object Composable : Construct<ClientUi>(
    requirements = listOf(
        predicate("is not a Screen") { declaration -> !Screen.test(declaration) },
        isAnnotatedWith("Composable"),
    ),
) {
    @Describe("A `[Name]ScreenContent` composable must be called from a `@Preview` composable in the same file")
    val screenContentPreview by rule {
        rationale(
            """
            A preview renders every state of a screen without a running app. The preview must
            live in the same file as the ScreenContent it renders — co-locating it keeps each
            screen's preview next to the screen, discoverable and maintained with it, instead of
            drifting into a single shared "screen previews" file.
            """.trimIndent(),
        )
        scope { scope, exempt ->
            val previewsByFile = scope.functions()
                .filter { it.hasAnnotationWithName("Preview") }
                .groupBy { it.containingFile.path }
            scope.functions()
                .filter { it.name.endsWith("ScreenContent") }
                .filter { it.resideInPackage("feature..client.ui..") }
                .filterNot { exempt(it) }
                .filterNot { fn ->
                    previewsByFile[fn.containingFile.path]
                        .orEmpty()
                        .any { preview -> preview.text.contains("${fn.name}(") }
                }
                .map { Violation(it, "no @Preview composable in the same file calls `${it.name}(`") }
        }
    }

    @Describe("Dialog primitives (`AlertDialog`, `BasicAlertDialog`, `DatePickerDialog`, `ModalBottomSheet`, `androidx.compose.ui.window.Dialog`) may only be invoked in a file that declares a dialog destination (one containing a `directOverlay` metadata marker)")
    val dialogPrimitivesOnlyInDialogDestinations by rule {
        rationale(
            """
            Dialogs are their own destinations: a `NavigationKey` rendered through Enro's overlay
            support, not an inline composable toggled by a boolean in screen state. Restricting
            dialog primitives to dialog-destination files makes embedded dialogs a build failure,
            not a review finding. Platform and design-system modules are exempt — they may define
            dialog primitives and wrappers.
            """.trimIndent(),
        )
        note("Detection is import-based: an import of any dialog primitive in a non-dialog-destination file is a violation, regardless of whether the call site is reached.")
        val dialogPrimitiveImports = listOf(
            "androidx.compose.material3.AlertDialog",
            "androidx.compose.material3.BasicAlertDialog",
            "androidx.compose.material3.DatePickerDialog",
            "androidx.compose.material3.ModalBottomSheet",
            "androidx.compose.ui.window.Dialog",
        )
        scope { scope, exempt ->
            scope.files
                .filter { it.isFeatureModule() && it.packagee?.name?.contains(".client.ui") == true }
                .filterNot { exempt(it) }
                .filter { file ->
                    file.imports.any { import -> dialogPrimitiveImports.any { import.name.startsWith(it) } }
                }
                .filterNot { file -> file.text.contains("directOverlay") }
                .map { Violation(it.path, "dialog primitive outside a dialog destination — dialogs are their own destinations (see ClientUi guidance)") }
        }
    }
}
