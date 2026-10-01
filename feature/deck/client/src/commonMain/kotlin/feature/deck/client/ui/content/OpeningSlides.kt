package feature.deck.client.ui.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import feature.deck.client.ui.CaretLine
import feature.deck.client.ui.Cards
import feature.deck.client.ui.CodeLanguage
import feature.deck.client.ui.Columns
import feature.deck.client.ui.LocalSlideFormat
import feature.deck.client.ui.LocalSlideStep
import feature.deck.client.ui.LoopingTitle
import feature.deck.client.ui.Pill
import feature.deck.client.ui.Reveal
import feature.deck.client.ui.SlideFormat
import feature.deck.client.ui.SlideFrame
import feature.deck.client.ui.rememberHighlightedCode
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme

@Composable
internal fun CoverSlide() {
    val typography = BackAgainTheme.slideTypography
    val colors = BackAgainTheme.colors
    val titles = @Composable {
        Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.md)) {
            Text(
                text = "Kotlin in Google Cloud:",
                style = typography.title.copy(fontSize = typography.title.fontSize * 0.85f, lineHeight = typography.title.lineHeight * 0.85f),
                color = colors.onSurfaceVariant,
            )
            LoopingTitle(title = "Browser,\nbackend,\nand *back again*")
        }
    }
    val name = @Composable {
        Text(
            text = "Isaac Udy",
            style = typography.body.copy(fontSize = typography.body.fontSize * 0.9f),
            color = colors.onSurfaceVariant,
        )
    }
    if (LocalSlideFormat.current == SlideFormat.Tall) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(BackAgainSpacing.lg),
        ) {
            Spacer(Modifier.weight(1f))
            titles()
            Spacer(Modifier.weight(1f))
            name()
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 72.dp, vertical = BackAgainSpacing.xxl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.xl),
        ) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Spacer(Modifier.weight(1f))
                titles()
                Spacer(Modifier.weight(1f))
                name()
            }
            JoinCode(size = 220.dp)
        }
    }
}

@Composable
internal fun HookSlide() {
    SlideFrame(title = "*this* is an application") {
        WithJoinCode {
            Cards(
                cards = listOf(
                    "the *browser*" to "Kotlin/Wasm with Compose UI",
                    "the *backend*" to "Kotlin/JVM on Google Cloud",
                    "*this* talk" to "Taking it apart, one step at a time",
                ),
                columns = 1,
            )
        }
    }
}

@Composable
internal fun WhoAmISlide() {
    SlideFrame(title = "*hi*, I'm Isaac") {
        WithJoinCode {
            Cards(
                cards = listOf(
                    "Independent engineer" to "",
                    "Google Developer Expert" to "",
                    "Lover of Kotlin" to "",
                ),
                columns = 1,
            )
        }
    }
}

private data class Target(val name: String, val runs: String, val ours: String?)

private val targets = listOf(
    Target("Kotlin/JVM", "Servers, desktop", ours = "Our backend"),
    Target("Kotlin/Android", "Android", ours = null),
    Target("Kotlin/JS", "Browser, Node.js", ours = null),
    Target("Kotlin/Wasm", "Browser", ours = "Our frontend"),
    Target("Kotlin/Native", "iOS, macOS, Linux, Windows", ours = null),
)

@Composable
internal fun KmpTargetsSlide() {
    val step = LocalSlideStep.current
    val typography = BackAgainTheme.slideTypography
    val tall = LocalSlideFormat.current == SlideFormat.Tall
    SlideFrame(title = "Kotlin has *five* targets") {
        Column {
            targets.forEach { target ->
                val picked = step >= 1
                CaretLine(active = picked && target.ours != null) {
                    Row(
                        modifier = Modifier.alpha(if (picked && target.ours == null) 0.45f else 1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.lg),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(target.name, style = typography.body, color = BackAgainTheme.colors.onSurface)
                            if (tall) Text(target.runs, style = typography.caption, color = BackAgainTheme.colors.onSurfaceVariant)
                        }
                        if (!tall) {
                            Text(
                                text = target.runs,
                                style = typography.caption,
                                color = BackAgainTheme.colors.onSurfaceVariant,
                                modifier = Modifier.weight(1.6f),
                            )
                        }
                        Reveal(visible = picked, modifier = Modifier.width(if (tall) 110.dp else 170.dp)) {
                            Pill(text = target.ours ?: "Not today", emphasised = target.ours != null)
                        }
                    }
                }
            }
        }
    }
}

private val sourceTree = listOf(
    "myApp/" to Tree.Plain,
    "├─ build.gradle.kts" to Tree.Plain,
    "└─ src/" to Tree.Plain,
    "   ├─ commonMain/   // everywhere" to Tree.Common,
    "   ├─ jvmMain/      // server only" to Tree.Jvm,
    "   ├─ wasmJsMain/   // browser only" to Tree.Wasm,
    "   ├─ androidMain/  // if you need it" to Tree.Unused,
    "   └─ iosMain/      // if you need it" to Tree.Unused,
)

private enum class Tree { Plain, Common, Jvm, Wasm, Unused }

@Composable
internal fun KmpHowSlide() {
    val step = LocalSlideStep.current
    SlideFrame(title = "Shared code, compiled *per target*") {
        Columns(
            start = {
                Column {
                    sourceTree.forEach { (line, kind) ->
                        val building = kind == Tree.Common && step >= 1 ||
                            kind == Tree.Jvm && step == 1 ||
                            kind == Tree.Wasm && step == 2
                        CaretLine(active = building, modifier = Modifier.alpha(if (kind == Tree.Unused) 0.4f else 1f)) {
                            Text(
                                text = rememberHighlightedCode(line, CodeLanguage.Kotlin),
                                style = BackAgainTheme.slideTypography.code,
                                softWrap = false,
                            )
                        }
                    }
                }
            },
            end = {
                Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.xl)) {
                    Reveal(visible = step >= 1) {
                        BuildRow(label = "The server", target = "jvmMain", output = ".jar")
                    }
                    Reveal(visible = step >= 2) {
                        BuildRow(label = "The browser", target = "wasmJsMain", output = ".wasm")
                    }
                }
            },
        )
    }
}

@Composable
private fun BuildRow(
    label: String,
    target: String,
    output: String,
) {
    val typography = BackAgainTheme.slideTypography
    Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm)) {
        Text(label, style = typography.body, color = BackAgainTheme.colors.onSurface)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
        ) {
            Pill("commonMain")
            Text("+", style = typography.caption, color = BackAgainTheme.colors.onSurfaceVariant)
            Pill(target)
            Text("→", style = typography.body, color = BackAgainTheme.codeColors.keyword)
            Text(output, style = typography.body, color = BackAgainTheme.colors.onSurface)
        }
    }
}
