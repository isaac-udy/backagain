package feature.deck.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import platform.design.BackAgainTheme

/**
 * Slide copy with a splash of the editor's syntax colours: `*a phrase*` in the keyword colour (the
 * asterisks are dropped), numbers in the number colour, and "a quotation" in the string colour.
 */
@Composable
internal fun highlighted(text: String): AnnotatedString {
    val colors = BackAgainTheme.codeColors
    return remember(text, colors) {
        buildAnnotatedString {
            var last = 0
            for (match in markup.findAll(text)) {
                append(text, last, match.range.first)
                val keyword = match.groups[1]
                when {
                    keyword != null -> withStyle(SpanStyle(color = colors.keyword)) { append(keyword.value) }
                    match.value.startsWith('"') -> withStyle(SpanStyle(color = colors.string)) { append(match.value) }
                    else -> withStyle(SpanStyle(color = colors.number)) { append(match.value) }
                }
                last = match.range.last + 1
            }
            append(text, last, text.length)
        }
    }
}

private val markup = Regex("""\*([^*]+)\*|"[^"]*"|\b\d+(?:[.,]\d+)*\b""")
