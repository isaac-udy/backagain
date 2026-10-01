package feature.deck.client.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.enro.ui.LocalNavigationAnimatedVisibilityScopeOrNull
import dev.enro.ui.LocalNavigationSharedTransitionScopeOrNull
import platform.design.BackAgainShapes
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme

/**
 * Marks this element as the same thing as the element with [key] on the neighbouring slide, so
 * moving between the two slides moves and scales one into the other instead of swapping them.
 *
 * Both ends are scaled to the moving bounds, never re-laid out. Where they lay out the same, leave
 * [isSmallEnd] null, and only the arriving one is drawn. Where they differ, say which end this is,
 * and the two cross-fade while the bounds are small: text scaled from a small layout up to a big
 * one's bounds is huge.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.sharedAcrossSlides(
    key: String,
    isSmallEnd: Boolean? = null,
): Modifier {
    // Absent outside a NavigationDisplay, as in previews.
    val sharedTransitionScope = LocalNavigationSharedTransitionScopeOrNull.current ?: return this
    val visibilityScope = LocalNavigationAnimatedVisibilityScopeOrNull.current ?: return this
    val early = tween<Float>(durationMillis = CROSS_FADE_MILLIS)
    val late = tween<Float>(durationMillis = CROSS_FADE_MILLIS, delayMillis = SHARED_BOUNDS_MILLIS - CROSS_FADE_MILLIS - 50)
    val (enter, exit) = when (isSmallEnd) {
        null -> EnterTransition.None to fadeOut(snap())
        true -> fadeIn(late) to fadeOut(early)
        false -> fadeIn(early) to fadeOut(late)
    }
    return with(sharedTransitionScope) {
        this@sharedAcrossSlides.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibilityScope,
            enter = enter,
            exit = exit,
            boundsTransform = { _, _ -> tween(SHARED_BOUNDS_MILLIS, easing = FastOutSlowInEasing) },
            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
        )
    }
}

private const val SHARED_BOUNDS_MILLIS = 500
private const val CROSS_FADE_MILLIS = 150

/**
 * The shared shape of a content slide: a title, then whatever the slide says. [headerEnd] sits
 * above the title, at the far end of the line on a wide slide.
 */
@Composable
internal fun SlideFrame(
    title: String,
    modifier: Modifier = Modifier,
    headerEnd: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tall = LocalSlideFormat.current == SlideFormat.Tall
    val inset = slideInset()
    CompositionLocalProvider(LocalCaretLineBleed provides CaretLineBleed(start = inset, end = inset)) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .then(if (tall) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = inset, vertical = if (tall) BackAgainSpacing.lg else 48.dp),
            verticalArrangement = Arrangement.spacedBy(if (tall) BackAgainSpacing.lg else BackAgainSpacing.xl),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm)) {
                if (headerEnd != null) {
                    Row {
                        if (!tall) Spacer(Modifier.weight(1f))
                        headerEnd()
                    }
                }
                Text(
                    text = highlighted(title),
                    style = BackAgainTheme.slideTypography.title,
                    color = BackAgainTheme.colors.onSurface,
                )
            }
            content()
        }
    }
}

/** Where in the talk a slide is, written as the line comment above a declaration. */
@Composable
internal fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "// $text",
        style = BackAgainTheme.slideTypography.code,
        color = BackAgainTheme.codeColors.comment,
        modifier = modifier,
    )
}

/** A short label in a rounded outline; [emphasised] fills it with the accent. */
@Composable
internal fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    emphasised: Boolean = false,
) {
    val colors = BackAgainTheme.colors
    Text(
        text = text,
        style = BackAgainTheme.slideTypography.caption,
        color = if (emphasised) colors.onAccent else colors.onSurfaceVariant,
        maxLines = 1,
        modifier = modifier
            .background(if (emphasised) colors.accent else Color.Transparent, BackAgainShapes.small)
            .border(1.dp, if (emphasised) colors.accent else colors.outline, BackAgainShapes.small)
            .padding(horizontal = BackAgainSpacing.sm, vertical = BackAgainSpacing.xs),
    )
}

/**
 * [cards] as [DiagramNode]s, in rows of [columns] on a wide slide and one column on a tall one. With
 * a [firstStep], card `i` appears at step `firstStep + i`; without one, they're all there at once.
 */
@Composable
internal fun Cards(
    cards: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    columns: Int = cards.size,
    firstStep: Int? = null,
    emphasised: Int? = null,
) {
    val step = LocalSlideStep.current
    val perRow = if (LocalSlideFormat.current == SlideFormat.Tall) 1 else columns
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
    ) {
        cards.withIndex().chunked(perRow).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
            ) {
                row.forEach { (index, card) ->
                    Reveal(
                        visible = firstStep == null || step >= firstStep + index,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    ) {
                        DiagramNode(
                            title = card.first,
                            detail = card.second,
                            emphasised = index == emphasised,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                repeat(perRow - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * Short headed points, one above the other: each a phrase and a line about it. With a [firstStep],
 * point `i` appears at step `firstStep + i`, and the newest sits on the caret line.
 */
@Composable
internal fun Points(
    points: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    firstStep: Int? = null,
) {
    val step = LocalSlideStep.current
    val typography = BackAgainTheme.slideTypography
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm)) {
        points.forEachIndexed { index, (point, detail) ->
            val at = firstStep?.plus(index)
            CaretLine(active = at != null && step == at) {
                Reveal(visible = at == null || step >= at) {
                    Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.xs)) {
                        Text(highlighted(point), style = typography.body, color = BackAgainTheme.colors.onSurface)
                        if (detail.isNotEmpty()) {
                            Text(highlighted(detail), style = typography.caption, color = BackAgainTheme.colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

/**
 * [code] with [points] beside it on a wide slide, or below it on a tall one. The code takes
 * [codeWeight] times the points' width, set in [besidePointsCodeStyle] so a dozen real lines fit
 * beside the points. [focus] is as [CodeBlock] has it.
 */
@Composable
internal fun CodeAndPoints(
    code: String,
    points: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    language: CodeLanguage = CodeLanguage.Kotlin,
    firstStep: Int? = null,
    codeWeight: Float = 2.3f,
    focus: Set<Int> = emptySet(),
) {
    BesidePoints(points = points, modifier = modifier, firstStep = firstStep, weight = codeWeight) {
        CodeBlock(code = code, language = language, style = besidePointsCodeStyle(), focus = focus)
    }
}

/** Code a size down from [CodeBlock]'s usual, [scale]d further if it needs to make room. */
@Composable
internal fun besidePointsCodeStyle(scale: Float = 1f): TextStyle {
    val code = BackAgainTheme.slideTypography.code
    return code.copy(fontSize = code.fontSize * 0.9f * scale, lineHeight = code.lineHeight * 0.9f * scale)
}

/**
 * [content] with [points] beside it on a wide slide, or below it on a tall one; [content] takes
 * [weight] times the points' width.
 */
@Composable
internal fun BesidePoints(
    points: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    firstStep: Int? = null,
    weight: Float = 2.3f,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (LocalSlideFormat.current == SlideFormat.Tall) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.lg)) {
            content()
            Points(points = points, firstStep = firstStep)
        }
    } else {
        val bleed = LocalCaretLineBleed.current
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.xxl)) {
            Column(
                modifier = Modifier.weight(weight),
                verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.lg),
                content = content,
            )
            Box(Modifier.weight(1f)) {
                CompositionLocalProvider(LocalCaretLineBleed provides bleed.copy(start = BackAgainSpacing.xxl / 2)) {
                    Points(points = points, firstStep = firstStep)
                }
            }
        }
    }
}

/** A numbered list, item `i` appearing at step `i`; item [highlight] sits on the caret line once it's there. */
@Composable
internal fun NumberedList(
    items: List<String>,
    modifier: Modifier = Modifier,
    highlight: Int? = null,
) {
    val step = LocalSlideStep.current
    val typography = BackAgainTheme.slideTypography
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            CaretLine(active = index == highlight && step >= index) {
                Reveal(visible = index <= step) {
                    Row(horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.lg)) {
                        Text(
                            text = "${index + 1}",
                            style = typography.body,
                            color = BackAgainTheme.codeColors.number,
                        )
                        Text(
                            text = highlighted(item),
                            style = typography.body,
                            color = BackAgainTheme.colors.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun slideInset(): Dp = if (LocalSlideFormat.current == SlideFormat.Tall) BackAgainSpacing.lg else 72.dp

/**
 * How far a caret line reaches past its content on each side. An editor's caret line runs the
 * width of the editor, so a line in a full-width block bleeds to the slide's edges, and a line in
 * one of two [Columns] stops halfway across the gap.
 */
private data class CaretLineBleed(val start: Dp, val end: Dp)

private val LocalCaretLineBleed = staticCompositionLocalOf { CaretLineBleed(0.dp, 0.dp) }

/** Paints the editor's caret-line band behind [content] while [active]: the line the slide is on. */
@Composable
internal fun CaretLine(
    active: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val band = BackAgainTheme.colors.caretLine
    val bleed = LocalCaretLineBleed.current
    val shown by animateFloatAsState(if (active) 1f else 0f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                if (shown > 0f) {
                    val start = bleed.start.toPx()
                    drawRect(
                        color = band.copy(alpha = band.alpha * shown),
                        topLeft = Offset(-start, 0f),
                        size = Size(size.width + start + bleed.end.toPx(), size.height),
                    )
                }
            }
            .padding(vertical = if (LocalSlideFormat.current == SlideFormat.Tall) BackAgainSpacing.sm else 12.dp),
    ) {
        content()
    }
}

/** [text], [highlighted], with the editor's blinking caret after its last character while [caret] is set. */
@Composable
internal fun CaretText(
    text: String,
    style: TextStyle,
    color: Color,
    caret: Boolean,
    modifier: Modifier = Modifier,
) {
    val highlightedText = highlighted(text)
    if (!caret) {
        Text(text = highlightedText, style = style, color = color, modifier = modifier)
        return
    }
    Text(
        text = buildAnnotatedString {
            append(highlightedText)
            appendInlineContent(CARET)
        },
        style = style,
        color = color,
        modifier = modifier,
        inlineContent = mapOf(
            CARET to InlineTextContent(Placeholder(0.25.em, 1.em, PlaceholderVerticalAlign.TextCenter)) { Caret() },
        ),
    )
}

@Composable
private fun Caret() {
    val blink = rememberInfiniteTransition()
    val alpha by blink.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 1_060
                1f at 529
                0f at 530
            },
        ),
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Box(
            Modifier
                .width(2.dp)
                .fillMaxHeight()
                .graphicsLayer { this.alpha = alpha }
                .background(BackAgainTheme.colors.caret),
        )
    }
}

private const val CARET = "caret"

/**
 * A title, and optionally a subtitle and a footer set as a string literal on the caret line. With
 * [loopBack], an arrow draws itself from the end of the title's last line round to its first.
 */
@Composable
internal fun TitleSlide(
    title: String,
    subtitle: String? = null,
    footer: String? = null,
    loopBack: Boolean = false,
) {
    val tall = LocalSlideFormat.current == SlideFormat.Tall
    val inset = if (tall) BackAgainSpacing.lg else 96.dp
    CompositionLocalProvider(LocalCaretLineBleed provides CaretLineBleed(start = inset, end = inset)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = inset, vertical = BackAgainSpacing.xxl),
            verticalArrangement = Arrangement.Center,
        ) {
            if (loopBack) {
                LoopingTitle(title = title)
            } else {
                Text(
                    text = highlighted(title),
                    style = BackAgainTheme.slideTypography.hero,
                    color = BackAgainTheme.colors.onSurface,
                )
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = BackAgainTheme.slideTypography.body,
                    color = BackAgainTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = BackAgainSpacing.lg),
                )
            }
            if (footer != null) {
                CaretLine(active = true, modifier = Modifier.padding(top = BackAgainSpacing.xl)) {
                    CaretText(
                        text = "\"$footer\"",
                        style = BackAgainTheme.slideTypography.code,
                        color = BackAgainTheme.codeColors.string,
                        caret = true,
                    )
                }
            }
        }
    }
}

/** [title] as the hero, with an arrow that curves out past its widest line and back to its first. */
@Composable
internal fun LoopingTitle(title: String) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val drawn = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        drawn.animateTo(1f, tween(durationMillis = 900, delayMillis = 500, easing = FastOutSlowInEasing))
    }
    val color = BackAgainTheme.codeColors.keyword
    Text(
        text = highlighted(title),
        style = BackAgainTheme.slideTypography.hero,
        color = BackAgainTheme.colors.onSurface,
        onTextLayout = { layout = it },
        modifier = Modifier.drawWithContent {
            drawContent()
            val text = layout ?: return@drawWithContent
            if (text.lineCount < 2 || drawn.value == 0f) return@drawWithContent
            val last = text.lineCount - 1
            val gap = 24.dp.toPx()
            val bulge = (0 until text.lineCount).maxOf { text.getLineRight(it) } + 72.dp.toPx()
            val from = Offset(text.getLineRight(last) + gap, lineMiddle(text, last))
            val to = Offset(text.getLineRight(0) + gap, lineMiddle(text, 0))
            val path = Path().apply {
                moveTo(from.x, from.y)
                cubicTo(bulge, from.y, bulge, to.y, to.x, to.y)
            }
            val measure = PathMeasure().apply { setPath(path, forceClosed = false) }
            val partial = Path().also { measure.getSegment(0f, measure.length * drawn.value, it, startWithMoveTo = true) }
            val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            drawPath(partial, color, style = stroke)
            if (drawn.value == 1f) {
                val head = 14.dp.toPx()
                val arrowhead = Path().apply {
                    moveTo(to.x + head, to.y - head * 0.7f)
                    lineTo(to.x, to.y)
                    lineTo(to.x + head, to.y + head * 0.7f)
                }
                drawPath(arrowhead, color, style = stroke)
            }
        },
    )
}

private fun lineMiddle(text: TextLayoutResult, line: Int): Float =
    (text.getLineTop(line) + text.getLineBottom(line)) / 2

/** Fades [content] in when [visible], keeping its space either way so the layout never jumps. */
@Composable
internal fun Reveal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress by animateFloatAsState(if (visible) 1f else 0f)
    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 12.dp.toPx()
        },
    ) {
        content()
    }
}

/**
 * Bullet `i` appears at step `i`, so a slide of `n` bullets has `n - 1` steps. The newest one sits
 * on the caret line.
 */
@Composable
internal fun Bullets(
    bullets: List<String>,
    modifier: Modifier = Modifier,
) {
    val step = LocalSlideStep.current
    Column(modifier = modifier) {
        bullets.forEachIndexed { index, bullet ->
            // The band sits outside the fade: a fading layer is drawn offscreen at the size of its
            // content, which would clip the band until the fade finished.
            CaretLine(active = index == step) {
                Reveal(visible = index <= step) {
                    CaretText(
                        text = bullet,
                        style = BackAgainTheme.slideTypography.body,
                        color = BackAgainTheme.colors.onSurface,
                        caret = index == step,
                    )
                }
            }
        }
    }
}

@Composable
internal fun BulletsSlide(
    title: String,
    bullets: List<String>,
    modifier: Modifier = Modifier,
) {
    SlideFrame(title = title, modifier = modifier) {
        Bullets(bullets = bullets)
    }
}

/**
 * Code as the editor shows it, on a card of its own: line numbers in a gutter, and the IDE's colours.
 *
 * What the slide isn't about can be played down: `«…»` greys out what it wraps, and the lines
 * numbered in [focus] sit on a band, so the eye goes to them first.
 */
@Composable
internal fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    language: CodeLanguage = CodeLanguage.Kotlin,
    lineNumbers: Boolean = true,
    style: TextStyle = BackAgainTheme.slideTypography.code,
    focus: Set<Int> = emptySet(),
) {
    val (plain, quiet) = remember(code) { withoutQuietMarkers(code) }
    val text = rememberHighlightedCode(plain, language, quiet)
    val band = BackAgainTheme.colors.accent.copy(alpha = 0.12f)
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackAgainTheme.colors.codeBlock, BackAgainShapes.medium)
            .drawBehind {
                val lines = layout ?: return@drawBehind
                val top = BackAgainSpacing.md.toPx()
                focus.filter { it in 1..lines.lineCount }.forEach { line ->
                    drawRect(
                        color = band,
                        topLeft = Offset(0f, top + lines.getLineTop(line - 1)),
                        size = Size(size.width, lines.getLineBottom(line - 1) - lines.getLineTop(line - 1)),
                    )
                }
            }
            .padding(BackAgainSpacing.md),
    ) {
        if (lineNumbers) {
            Text(
                text = (1..plain.lines().size).joinToString("\n"),
                style = style,
                color = BackAgainTheme.colors.lineNumber,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(end = BackAgainSpacing.lg),
            )
        }
        Text(
            text = text,
            style = style,
            softWrap = false,
            onTextLayout = { layout = it },
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        )
    }
}

/** [code] without its `«…»` markers, and where the text they wrapped now sits. */
private fun withoutQuietMarkers(code: String): Pair<String, List<IntRange>> {
    val plain = StringBuilder()
    val quiet = mutableListOf<IntRange>()
    var start = -1
    code.forEach { char ->
        when (char) {
            '«' -> start = plain.length
            '»' -> quiet += start until plain.length
            else -> plain.append(char)
        }
    }
    return plain.toString() to quiet
}

@Composable
internal fun CodeSlide(
    title: String,
    code: String,
    caption: String? = null,
    language: CodeLanguage = CodeLanguage.Kotlin,
) {
    SlideFrame(title = title) {
        CodeBlock(code = code, language = language)
        if (caption != null) {
            Text(
                text = caption,
                style = BackAgainTheme.slideTypography.caption,
                color = BackAgainTheme.colors.onSurfaceVariant,
            )
        }
    }
}

/** Two columns side by side on a wide slide, one above the other on a tall one. */
@Composable
internal fun Columns(
    start: @Composable () -> Unit,
    end: @Composable () -> Unit,
) {
    if (LocalSlideFormat.current == SlideFormat.Tall) {
        Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.xl)) {
            start()
            end()
        }
    } else {
        val bleed = LocalCaretLineBleed.current
        val halfGap = BackAgainSpacing.xxl / 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.xxl),
        ) {
            Box(Modifier.weight(1f)) {
                CompositionLocalProvider(LocalCaretLineBleed provides bleed.copy(end = halfGap)) { start() }
            }
            Box(Modifier.weight(1f)) {
                CompositionLocalProvider(LocalCaretLineBleed provides bleed.copy(start = halfGap)) { end() }
            }
        }
    }
}

/** A box in a diagram, drawn as an IDE panel: a name, and a line about what it does. */
@Composable
internal fun DiagramNode(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    emphasised: Boolean = false,
) {
    val colors = BackAgainTheme.colors
    Column(
        modifier = modifier
            .background(colors.surface, BackAgainShapes.medium)
            .border(if (emphasised) 2.dp else 1.dp, if (emphasised) colors.accent else colors.outline, BackAgainShapes.medium)
            .padding(BackAgainSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
    ) {
        Text(
            text = highlighted(title),
            style = BackAgainTheme.slideTypography.body,
            color = colors.onSurface,
        )
        if (detail.isNotEmpty()) {
            Text(
                text = detail,
                style = BackAgainTheme.slideTypography.caption,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun DiagramArrow(
    label: String = "",
    modifier: Modifier = Modifier,
) {
    val vertical = LocalSlideFormat.current == SlideFormat.Tall
    Column(
        modifier = modifier.widthIn(max = 150.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = BackAgainTheme.slideTypography.caption,
                color = BackAgainTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = if (vertical) "↓" else "→",
            style = BackAgainTheme.slideTypography.title,
            color = BackAgainTheme.codeColors.keyword,
        )
    }
}

/** A flow left to right on a wide slide, top to bottom on a tall one; node `i` appears at step `i`. */
@Composable
internal fun FlowDiagram(
    nodes: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    arrowLabels: List<String> = emptyList(),
) {
    val step = LocalSlideStep.current
    if (LocalSlideFormat.current == SlideFormat.Tall) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            nodes.forEachIndexed { index, (title, detail) ->
                if (index > 0) {
                    Reveal(visible = index <= step) {
                        DiagramArrow(label = arrowLabels.getOrElse(index - 1) { "" })
                    }
                }
                Reveal(visible = index <= step, modifier = Modifier.fillMaxWidth()) {
                    DiagramNode(title = title, detail = detail, emphasised = index == step, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            nodes.forEachIndexed { index, (title, detail) ->
                if (index > 0) {
                    Reveal(visible = index <= step) {
                        DiagramArrow(label = arrowLabels.getOrElse(index - 1) { "" })
                    }
                }
                Reveal(visible = index <= step, modifier = Modifier.weight(1f)) {
                    DiagramNode(title = title, detail = detail, emphasised = index == step, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

/** A number worth reading from the back of the room, and what it counts. */
@Composable
internal fun Stat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = BackAgainTheme.slideTypography.hero,
            color = BackAgainTheme.codeColors.number,
        )
        Text(
            text = label,
            style = BackAgainTheme.slideTypography.caption,
            color = BackAgainTheme.colors.onSurfaceVariant,
        )
    }
}
