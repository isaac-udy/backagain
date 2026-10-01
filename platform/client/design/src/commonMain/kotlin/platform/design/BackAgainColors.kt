package platform.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The design system's semantic colour roles.
 *
 * Roles are named for the job they do, not the colour they are: a screen asks for `surface` and
 * `onSurface`, never for "grey 100". That is what lets a palette swap without touching call sites,
 * and it is why a theme *is* a [BackAgainColors] instance rather than an enum of theme names.
 *
 * Both palettes follow the IntelliJ New UI themes, so the deck reads as an editor to a room of
 * people who spend their day in one. Each device picks its own; see [BackAgainTheme.mode].
 */
@Immutable
data class BackAgainColors(
    /** The page behind everything: the editor. */
    val background: Color,
    /** Raised areas that sit on [background]: panels, balloons, bars. */
    val surface: Color,
    /** Primary content on [surface] or [background]. */
    val onSurface: Color,
    /** Secondary content: supporting text, inactive icons. Must stay legible on [surface]. */
    val onSurfaceVariant: Color,
    /** The one colour that draws the eye. Used for primary actions, selection and emphasis. */
    val accent: Color,
    /** Content on [accent]. */
    val onAccent: Color,
    /** Something happening right now: the live indicator, a connected socket, your own vote. */
    val live: Color,
    /** Hairlines, dividers and borders. */
    val outline: Color,
    /** Destructive and failure states. */
    val error: Color,
    /** Content on [error]. */
    val onError: Color,
    /** The full-width band behind the line the caret is on: what the slide is saying right now. */
    val caretLine: Color,
    /** The caret itself. */
    val caret: Color,
    /** Line numbers in a code gutter. */
    val lineNumber: Color,
    /** Behind a block of code: a shade darker than [background], so the code stands apart from the slide. */
    val codeBlock: Color,
) {
    val isDark: Boolean get() = background.red + background.green + background.blue < 1.5f

    companion object {
        val Light: BackAgainColors = BackAgainColors(
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF7F8FA),
            onSurface = Color(0xFF080808),
            onSurfaceVariant = Color(0xFF6C707E),
            accent = Color(0xFF3574F0),
            onAccent = Color(0xFFFFFFFF),
            live = Color(0xFF208A3C),
            outline = Color(0xFFDFE1E5),
            error = Color(0xFFDB3B4B),
            onError = Color(0xFFFFFFFF),
            caretLine = Color(0xFFF5F8FE),
            caret = Color(0xFF000000),
            lineNumber = Color(0xFFAEB3C2),
            codeBlock = Color(0xFFF0F1F4),
        )

        val Dark: BackAgainColors = BackAgainColors(
            background = Color(0xFF1E1F22),
            surface = Color(0xFF2B2D30),
            onSurface = Color(0xFFDFE1E5),
            onSurfaceVariant = Color(0xFF9DA0A8),
            accent = Color(0xFF3574F0),
            onAccent = Color(0xFFFFFFFF),
            live = Color(0xFF5FB865),
            outline = Color(0xFF43454A),
            error = Color(0xFFF75464),
            onError = Color(0xFFFFFFFF),
            caretLine = Color(0xFF26282E),
            caret = Color(0xFFCED0D6),
            lineNumber = Color(0xFF4B5059),
            codeBlock = Color(0xFF17181A),
        )
    }
}
