package feature.deck.client.ui.content

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import feature.deck.client.ui.LocalSlideFormat
import feature.deck.client.ui.SlideFormat
import feature.deck.client.ui.highlighted
import platform.design.BackAgainShapes
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme

/**
 * The talk's map: the browser, the shared contract and the backend, joined in the order a vote
 * travels, with the way back running from the backend round to the browser. [stop] highlights
 * where the vote is: 1 the browser, 2 the hop to the contract, 3 the hop to the backend, 4 the way
 * back; 0 highlights nothing.
 *
 * [onGoogleCloud] draws the same trip inside Google Cloud, naming what runs where.
 */
@Composable
internal fun RoundTripMap(
    stop: Int,
    modifier: Modifier = Modifier,
    returnLabel: String = "…and back again",
    onGoogleCloud: Boolean = false,
) {
    val browser = @Composable { nodeModifier: Modifier ->
        MapNode(
            title = "Browser",
            chips = listOf("Kotlin/Wasm", "Compose/Wasm"),
            emphasised = stop == 1 || stop == 4,
            modifier = nodeModifier,
        )
    }
    val contract = @Composable { nodeModifier: Modifier ->
        MapNode(
            title = "Contract",
            chips = listOf("Kotlin/Common"),
            emphasised = stop == 2,
            modifier = nodeModifier,
        )
    }
    val backend = @Composable { nodeModifier: Modifier ->
        MapNode(
            title = "Backend",
            chips = if (onGoogleCloud) listOf("Kotlin on *Cloud Run*", "Postgres on *Cloud SQL*") else listOf("Kotlin/JVM", "Postgres"),
            emphasised = stop == 3 || onGoogleCloud,
            modifier = nodeModifier,
        )
    }
    val map = @Composable {
        if (LocalSlideFormat.current == SlideFormat.Tall) {
            TallMap(stop, returnLabel, browser, contract, backend)
        } else {
            WideMap(stop, returnLabel, browser, contract, backend)
        }
    }
    if (onGoogleCloud) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .border(2.dp, BackAgainTheme.colors.outline, BackAgainShapes.large)
                .padding(BackAgainSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
        ) {
            Text(
                text = "Google Cloud",
                style = BackAgainTheme.slideTypography.body,
                color = BackAgainTheme.codeColors.keyword,
            )
            map()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) { map() }
    }
}

@Composable
private fun WideMap(
    stop: Int,
    returnLabel: String,
    browser: @Composable (Modifier) -> Unit,
    contract: @Composable (Modifier) -> Unit,
    backend: @Composable (Modifier) -> Unit,
) {
    val weights = listOf(1f, 1f, 1.3f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            browser(Modifier.weight(weights[0]).fillMaxHeight())
            MapArrow(active = stop == 2, modifier = Modifier.width(ArrowWidth))
            contract(Modifier.weight(weights[1]).fillMaxHeight())
            MapArrow(active = stop == 3, modifier = Modifier.width(ArrowWidth))
            backend(Modifier.weight(weights[2]).fillMaxHeight())
        }
        Box(modifier = Modifier.fillMaxWidth().height(ReturnDepth)) {
            val color = returnColor(active = stop == 4)
            val width = if (stop == 4) 4.dp else 2.dp
            Canvas(Modifier.fillMaxSize()) {
                val arrows = ArrowWidth.toPx() * 2
                val unit = (size.width - arrows) / weights.sum()
                val from = size.width - unit * weights[2] / 2
                val to = unit * weights[0] / 2
                val bottom = size.height / 2
                val path = Path().apply {
                    moveTo(from, 0f)
                    lineTo(from, bottom)
                    lineTo(to, bottom)
                    lineTo(to, 0f)
                }
                drawReturn(path, color, width.toPx(), tip = Offset(to, 0f), pointing = Offset(0f, -1f))
            }
            ReturnLabel(
                text = returnLabel,
                active = stop == 4,
                // On the line, breaking it.
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun TallMap(
    stop: Int,
    returnLabel: String,
    browser: @Composable (Modifier) -> Unit,
    contract: @Composable (Modifier) -> Unit,
    backend: @Composable (Modifier) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm)) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                browser(Modifier.fillMaxWidth())
                MapArrow(active = stop == 2)
                contract(Modifier.fillMaxWidth())
                MapArrow(active = stop == 3)
                backend(Modifier.fillMaxWidth())
            }
            val color = returnColor(active = stop == 4)
            val width = if (stop == 4) 3.dp else 2.dp
            Canvas(Modifier.width(36.dp).fillMaxHeight()) {
                val inset = 32.dp.toPx()
                val side = size.width * 0.6f
                val path = Path().apply {
                    moveTo(0f, size.height - inset)
                    lineTo(side, size.height - inset)
                    lineTo(side, inset)
                    lineTo(0f, inset)
                }
                drawReturn(path, color, width.toPx(), tip = Offset(0f, inset), pointing = Offset(-1f, 0f))
            }
        }
        ReturnLabel(text = returnLabel, active = stop == 4, modifier = Modifier.align(Alignment.End))
    }
}

@Composable
private fun returnColor(active: Boolean): Color =
    if (active) BackAgainTheme.codeColors.keyword else BackAgainTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)

private fun DrawScope.drawReturn(
    path: Path,
    color: Color,
    width: Float,
    tip: Offset,
    pointing: Offset,
) {
    val stroke = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawPath(path, color, style = stroke)
    val head = width * 3.5f
    // The two barbs sit behind the tip, either side of the direction it points.
    val back = Offset(-pointing.x * head, -pointing.y * head)
    val across = Offset(-pointing.y * head * 0.7f, pointing.x * head * 0.7f)
    val arrowhead = Path().apply {
        moveTo(tip.x + back.x + across.x, tip.y + back.y + across.y)
        lineTo(tip.x, tip.y)
        lineTo(tip.x + back.x - across.x, tip.y + back.y - across.y)
    }
    drawPath(arrowhead, color, style = stroke)
}

@Composable
private fun ReturnLabel(
    text: String,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = BackAgainTheme.slideTypography.caption,
        color = if (active) BackAgainTheme.codeColors.keyword else BackAgainTheme.colors.onSurfaceVariant,
        modifier = modifier
            .background(BackAgainTheme.colors.background)
            .padding(horizontal = BackAgainSpacing.md),
    )
}

@Composable
private fun MapArrow(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val vertical = LocalSlideFormat.current == SlideFormat.Tall
    Text(
        text = if (vertical) "↓" else "→",
        style = BackAgainTheme.slideTypography.title,
        color = if (active) BackAgainTheme.codeColors.keyword else BackAgainTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

/** A box on the map: a name, and the [chips] of what's inside it. */
@Composable
private fun MapNode(
    title: String,
    chips: List<String>,
    modifier: Modifier = Modifier,
    emphasised: Boolean = false,
) {
    val colors = BackAgainTheme.colors
    val typography = BackAgainTheme.slideTypography
    Column(
        modifier = modifier
            .background(colors.surface, BackAgainShapes.medium)
            .border(if (emphasised) 2.dp else 1.dp, if (emphasised) colors.accent else colors.outline, BackAgainShapes.medium)
            .padding(BackAgainSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
    ) {
        Text(text = title, style = typography.body, color = colors.onSurface)
        chips.forEach { chip ->
            Text(
                text = highlighted(chip),
                style = typography.caption,
                color = colors.onSurface,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background, BackAgainShapes.small)
                    .border(1.dp, colors.outline, BackAgainShapes.small)
                    .padding(horizontal = BackAgainSpacing.md, vertical = BackAgainSpacing.sm),
            )
        }
    }
}

private val ArrowWidth = 72.dp
private val ReturnDepth = 72.dp
