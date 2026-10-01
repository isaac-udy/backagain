package platform.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalBackAgainColors = staticCompositionLocalOf { BackAgainColors.Light }
private val LocalCodeColors = staticCompositionLocalOf { CodeColors.Light }
private val LocalBackAgainThemeMode = staticCompositionLocalOf { BackAgainThemeMode(isDark = false) {} }
private val LocalBackAgainTypography = staticCompositionLocalOf { BackAgainTypography.from(BackAgainFonts.System) }
private val LocalBackAgainFonts = staticCompositionLocalOf { BackAgainFonts.System }
private val LocalSlideTypography = staticCompositionLocalOf { SlideTypography.from(BackAgainFonts.System) }

/**
 * Installs a palette and typeface set, and wraps a [MaterialTheme] derived from them.
 *
 * The MaterialTheme wrapper is not optional decoration. Raw material3 internals — text-field
 * decoration, dividers, `LocalContentColor`, ripple — read the material theme, so without it those
 * details land on material's defaults and quietly disagree with the tokens. Wrapping here means
 * call sites never wrap twice.
 *
 * [colors] has no default **on purpose**: the sibling overload defaults everything, so giving this
 * one a default would make a bare `BackAgainTheme { }` ambiguous. You reach for this overload precisely
 * when you want to force a palette or inject fonts.
 */
@Composable
fun BackAgainTheme(
    colors: BackAgainColors,
    fonts: BackAgainFonts = BackAgainFonts.System,
    codeColors: CodeColors = if (colors.isDark) CodeColors.Dark else CodeColors.Light,
    content: @Composable () -> Unit,
) {
    val typography = remember(fonts) { BackAgainTypography.from(fonts) }
    val slideTypography = remember(fonts) { SlideTypography.from(fonts) }
    val colorScheme = if (colors.isDark) darkColorScheme() else lightColorScheme()
    CompositionLocalProvider(
        LocalBackAgainColors provides colors,
        LocalCodeColors provides codeColors,
        LocalBackAgainFonts provides fonts,
        LocalBackAgainTypography provides typography,
        LocalSlideTypography provides slideTypography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme.copy(
                primary = colors.accent,
                onPrimary = colors.onAccent,
                background = colors.background,
                onBackground = colors.onSurface,
                surface = colors.surface,
                onSurface = colors.onSurface,
                surfaceVariant = colors.surface,
                // Dialogs, menus, sheets and cards paint their containers with these roles; left
                // unset they keep material's light defaults under either palette.
                surfaceContainerLowest = colors.surface,
                surfaceContainerLow = colors.surface,
                surfaceContainer = colors.surface,
                surfaceContainerHigh = colors.surface,
                surfaceContainerHighest = colors.surface,
                surfaceBright = colors.surface,
                surfaceDim = colors.surface,
                onSurfaceVariant = colors.onSurfaceVariant,
                outline = colors.outline,
                error = colors.error,
                onError = colors.onError,
            ),
            typography = Typography(
                displaySmall = typography.display,
                titleLarge = typography.title,
                bodyLarge = typography.body,
                labelLarge = typography.label,
                bodySmall = typography.caption,
            ),
            content = content,
        )
    }
}

/**
 * What the app root uses: the bundled typefaces, and whichever palette this device last chose,
 * following the system setting until it has chosen one. The choice is per device on purpose: the
 * projector, the presenter's phone and every phone in the room each keep their own.
 */
@Composable
fun BackAgainTheme(
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    var chosen by remember { mutableStateOf(ThemePreference.load()) }
    val dark = chosen ?: systemDark
    val mode = remember(dark) {
        BackAgainThemeMode(isDark = dark) {
            ThemePreference.save(!dark)
            chosen = !dark
        }
    }
    CompositionLocalProvider(LocalBackAgainThemeMode provides mode) {
        BackAgainTheme(
            colors = if (dark) BackAgainColors.Dark else BackAgainColors.Light,
            fonts = BackAgainFonts.bundled(),
            content = content,
        )
    }
}

/** Which palette this device shows, and how to switch to the other one. */
@Stable
class BackAgainThemeMode(
    val isDark: Boolean,
    val toggle: () -> Unit,
)

/** Switches [content] to the portrait-phone slide type scale. */
@Composable
fun ProvideCompactSlideTypography(content: @Composable () -> Unit) {
    val fonts = LocalBackAgainFonts.current
    val compact = remember(fonts) { SlideTypography.compactFrom(fonts) }
    CompositionLocalProvider(LocalSlideTypography provides compact, content = content)
}

/**
 * Reads the tokens in effect.
 *
 * This is the **only** way feature code should reach tokens — `BackAgainTheme.colors.accent`, never a
 * literal and never a direct `BackAgainColors.Light` reference, which would ignore the active palette.
 */
object BackAgainTheme {
    val colors: BackAgainColors
        @Composable @ReadOnlyComposable get() = LocalBackAgainColors.current

    val codeColors: CodeColors
        @Composable @ReadOnlyComposable get() = LocalCodeColors.current

    /** Only the app-root [BackAgainTheme] can switch palettes; under a forced palette, toggling does nothing. */
    val mode: BackAgainThemeMode
        @Composable @ReadOnlyComposable get() = LocalBackAgainThemeMode.current

    val typography: BackAgainTypography
        @Composable @ReadOnlyComposable get() = LocalBackAgainTypography.current

    val fonts: BackAgainFonts
        @Composable @ReadOnlyComposable get() = LocalBackAgainFonts.current

    val slideTypography: SlideTypography
        @Composable @ReadOnlyComposable get() = LocalSlideTypography.current

    /** Requires an enclosing [ProvideBackAgainViewport]; falls back to [BackAgainViewport.Default]. */
    val viewport: BackAgainViewport
        @Composable @ReadOnlyComposable get() = LocalBackAgainViewport.current
}
