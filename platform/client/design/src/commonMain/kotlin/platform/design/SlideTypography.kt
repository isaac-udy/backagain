package platform.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The type scale for slides. [from] sizes are for a 1280 × 720 dp canvas that is scaled to whatever
 * landscape screen shows it, so they stay in proportion from a laptop to a projector. [compactFrom]
 * is for a portrait phone, where the slide reflows into a single column instead.
 */
@Immutable
data class SlideTypography(
    /** A title slide or a single statement. */
    val hero: TextStyle,
    /** The heading of a content slide. */
    val title: TextStyle,
    /** Bullets and running text. */
    val body: TextStyle,
    /** Labels and small print. */
    val caption: TextStyle,
    /** Code blocks. */
    val code: TextStyle,
) {
    companion object {
        fun from(fonts: BackAgainFonts): SlideTypography = SlideTypography(
            hero = TextStyle(
                fontFamily = fonts.display,
                fontSize = 76.sp,
                lineHeight = 84.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.5).sp,
            ),
            title = TextStyle(
                fontFamily = fonts.display,
                fontSize = 48.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
            ),
            body = TextStyle(
                fontFamily = fonts.body,
                fontSize = 30.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Normal,
            ),
            caption = TextStyle(
                fontFamily = fonts.body,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Medium,
            ),
            code = TextStyle(
                fontFamily = fonts.mono,
                fontSize = 21.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Normal,
            ),
        )

        fun compactFrom(fonts: BackAgainFonts): SlideTypography = from(fonts).let { wide ->
            SlideTypography(
                hero = wide.hero.copy(fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.5).sp),
                title = wide.title.copy(fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = 0.sp),
                body = wide.body.copy(fontSize = 18.sp, lineHeight = 27.sp),
                caption = wide.caption.copy(fontSize = 14.sp, lineHeight = 20.sp),
                code = wide.code.copy(fontSize = 12.sp, lineHeight = 18.sp),
            )
        }
    }
}
