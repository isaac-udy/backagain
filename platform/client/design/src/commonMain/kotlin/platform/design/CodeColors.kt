package platform.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Syntax colours for code on slides: IntelliJ's own Kotlin colours, for each palette. */
@Immutable
data class CodeColors(
    val plain: Color,
    val keyword: Color,
    /** A function's name where it is declared. */
    val function: Color,
    /** A property read through a dot, and constants. */
    val property: Color,
    val string: Color,
    val number: Color,
    val annotation: Color,
    val comment: Color,
) {
    companion object {
        val Dark: CodeColors = CodeColors(
            plain = Color(0xFFBCBEC4),
            keyword = Color(0xFFCF8E6D),
            function = Color(0xFF56A8F5),
            property = Color(0xFFC77DBB),
            string = Color(0xFF6AAB73),
            number = Color(0xFF2AACB8),
            annotation = Color(0xFFB3AE60),
            comment = Color(0xFF7A7E85),
        )

        val Light: CodeColors = CodeColors(
            plain = Color(0xFF080808),
            keyword = Color(0xFF0033B3),
            function = Color(0xFF00627A),
            property = Color(0xFF871094),
            string = Color(0xFF067D17),
            number = Color(0xFF1750EB),
            annotation = Color(0xFF9E880D),
            comment = Color(0xFF8C8C8C),
        )
    }
}
