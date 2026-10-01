package feature.deck.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import platform.design.BackAgainTheme

/**
 * Close enough to the IDE's own highlighting that the code reads as Kotlin at a glance: keywords,
 * strings, numbers, annotations and comments, plus Kotlin's declared function names, properties
 * read through a dot, and constants. Types stay plain, as they do in the IDE. The [quiet] ranges
 * are greyed out instead, whatever they hold.
 */
@Composable
internal fun rememberHighlightedCode(
    code: String,
    language: CodeLanguage,
    quiet: List<IntRange> = emptyList(),
): AnnotatedString {
    val colors = BackAgainTheme.codeColors
    val quietColor = BackAgainTheme.colors.lineNumber
    return remember(code, language, quiet, colors, quietColor) {
        val keywords = when (language) {
            CodeLanguage.Kotlin -> kotlinKeywords
            CodeLanguage.Terraform -> terraformKeywords
            CodeLanguage.Yaml -> yamlKeywords
            CodeLanguage.Sql -> sqlKeywords
        }
        val comment = when (language) {
            CodeLanguage.Kotlin -> "//[^\\n]*"
            CodeLanguage.Sql -> "--[^\\n]*"
            else -> "#[^\\n]*"
        }
        val tokens = Regex("""($comment)|("(?:\\.|[^"\\])*")|(@[A-Za-z_][\w.]*)|(\b\d+(?:\.\d+)?\b)|(\b[A-Za-z_][\w-]*\b)""")
        val declaredFunctions = if (language == CodeLanguage.Kotlin) {
            functionDeclaration.findAll(code).mapNotNull { it.groups[1]?.range?.first }.toSet()
        } else {
            emptySet()
        }

        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.plain)) {
                var index = 0
                for (match in tokens.findAll(code)) {
                    append(code.substring(index, match.range.first))
                    val (commentText, string, annotation, number, word) = match.destructured
                    val before = code.getOrNull(match.range.first - 1)
                    val after = code.getOrNull(match.range.last + 1)
                    val style = when {
                        quiet.any { match.range.first in it } -> null
                        commentText.isNotEmpty() -> SpanStyle(color = colors.comment)
                        string.isNotEmpty() -> SpanStyle(color = colors.string)
                        annotation.isNotEmpty() -> SpanStyle(color = colors.annotation)
                        number.isNotEmpty() -> SpanStyle(color = colors.number)
                        word in keywords || language == CodeLanguage.Sql && word.uppercase() in keywords ->
                            SpanStyle(color = colors.keyword)
                        language != CodeLanguage.Kotlin -> null
                        match.range.first in declaredFunctions -> SpanStyle(color = colors.function)
                        word.length > 1 && word.all { it.isUpperCase() || it == '_' || it.isDigit() } ->
                            SpanStyle(color = colors.property, fontStyle = FontStyle.Italic)
                        before == '.' && word.first().isLowerCase() && after != '(' && after != '{' && after != ' ' ->
                            SpanStyle(color = colors.property)
                        else -> null
                    }
                    if (style == null) append(match.value) else withStyle(style) { append(match.value) }
                    index = match.range.last + 1
                }
                append(code.substring(index))
            }
            quiet.forEach { addStyle(SpanStyle(color = quietColor), it.first, it.last + 1) }
        }
    }
}

/** The name in `fun name(` or `fun Receiver.name(`. */
private val functionDeclaration = Regex("""\bfun\s+(?:<[^>]*>\s*)?(?:[A-Za-z_][\w<>?, ]*\.)?([A-Za-z_]\w*)\s*\(""")

private val kotlinKeywords = setOf(
    "fun", "val", "var", "class", "object", "interface", "internal", "private", "override",
    "suspend", "return", "if", "else", "when", "is", "in", "for", "while", "import", "package",
    "data", "sealed", "enum", "companion", "true", "false", "null", "this", "by", "throw", "try",
    "catch", "const",
)

private val terraformKeywords = setOf(
    "resource", "variable", "module", "data", "output", "locals", "provider", "terraform",
    "true", "false", "dynamic", "for_each",
)

private val yamlKeywords = setOf(
    "on", "jobs", "steps", "uses", "with", "run", "name", "env", "needs", "permissions",
    "environment", "runs-on",
)

private val sqlKeywords = setOf(
    "CREATE", "TABLE", "PRIMARY", "KEY", "NOT", "NULL", "REFERENCES", "DEFAULT", "UNIQUE", "INDEX",
    "ON", "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE",
)
