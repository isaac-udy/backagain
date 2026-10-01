package platform.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import platform.design.resources.Res
import platform.design.resources.jetbrains_mono

/**
 * The typeface slots the type scale is built from.
 *
 * Slots are roles, not typeface names, so [BackAgainTypography] can be built against any concrete set.
 */
@Immutable
data class BackAgainFonts(
    /** Large, low-frequency text: display and title roles. */
    val display: FontFamily,
    /** Running text and UI labels. */
    val body: FontFamily,
    /** Code, identifiers, and anything that must align in columns. */
    val mono: FontFamily,
) {
    companion object {
        /** The platform's own faces: what renders before the bundled fonts have loaded. */
        val System: BackAgainFonts = BackAgainFonts(
            display = FontFamily.Default,
            body = FontFamily.Default,
            mono = FontFamily.Monospace,
        )

        /**
         * JetBrains Mono in every slot, as the editor sets it; bundled as a variable font (licence in
         * `design-system/fonts/`). On web the file downloads after the first frame, so text redraws
         * once it arrives.
         */
        @Composable
        fun bundled(): BackAgainFonts {
            val mono = FontFamily(
                variableWeights.map { weight -> Font(Res.font.jetbrains_mono, weight, variationSettings = weightVariation(weight)) },
            )
            return BackAgainFonts(display = mono, body = mono, mono = mono)
        }

        private val variableWeights = listOf(
            FontWeight.Normal,
            FontWeight.Medium,
            FontWeight.SemiBold,
            FontWeight.Bold,
        )

        private fun weightVariation(weight: FontWeight) =
            FontVariation.Settings(FontVariation.weight(weight.weight))
    }
}
