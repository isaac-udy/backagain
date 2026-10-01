package feature.deck.client.ui.content

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import feature.deck.client.ui.BesidePoints
import feature.deck.client.ui.CodeAndPoints
import feature.deck.client.ui.CodeBlock
import feature.deck.client.ui.besidePointsCodeStyle
import feature.deck.client.ui.CodeLanguage
import feature.deck.client.ui.DiagramNode
import feature.deck.client.ui.LocalSlideFormat
import feature.deck.client.ui.LocalSlideStep
import feature.deck.client.ui.Pill
import feature.deck.client.ui.PollOptions
import feature.deck.client.ui.Reveal
import feature.deck.client.ui.SlideFormat
import feature.deck.client.ui.SlideFrame
import feature.deck.client.ui.highlighted
import feature.deck.client.ui.rememberHighlightedCode
import feature.deck.client.resources.Res
import feature.deck.client.resources.leegaa
import feature.deck.client.resources.reglyph
import feature.deck.client.resources.wool_online
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import feature.live.Polls
import platform.design.BackAgainShapes
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme

@Composable
internal fun MapSlide(
    title: String,
    stop: Int,
    returnLabel: String = "…and back again",
) {
    SlideFrame(title = title) {
        RoundTripMap(stop = stop, returnLabel = returnLabel)
    }
}

@Composable
internal fun KmpWasmSlide() {
    val webNative = @Composable { modifier: Modifier ->
        ToolkitPanel(
            label = "Web-native",
            title = "Kotlin/JS + a web framework",
            chips = listOf("React", "Compose HTML"),
            points = listOf("HTML", "CSS", "Web-only"),
            modifier = modifier,
        )
    }
    val compose = @Composable { modifier: Modifier ->
        ToolkitPanel(
            label = "This deck",
            title = "*Compose Multiplatform*",
            chips = listOf("Android", "iOS", "Desktop", "Web"),
            points = listOf("Canvas", "Shared UI", "Familiar to Android devs"),
            emphasised = true,
            modifier = modifier,
        )
    }
    val woolOnline = @Composable { modifier: Modifier ->
        ProjectCard(name = "Wool Online", detail = "Live", logo = ProjectLogo.WoolOnline, modifier = modifier)
    }
    val composeProjects = @Composable { modifier: Modifier ->
        Row(
            modifier = modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
        ) {
            ProjectCard(name = "Reglyph", detail = "Early access", logo = ProjectLogo.Reglyph, modifier = Modifier.weight(1f).fillMaxHeight())
            ProjectCard(name = "Leegaa", detail = "16 October", logo = ProjectLogo.Leegaa, modifier = Modifier.weight(1f).fillMaxHeight())
        }
    }
    SlideFrame(title = "*Two* ways to draw a web UI") {
        if (LocalSlideFormat.current == SlideFormat.Tall) {
            webNative(Modifier)
            woolOnline(Modifier.fillMaxWidth())
            compose(Modifier)
            composeProjects(Modifier.fillMaxWidth())
        } else {
            // Two columns whose panels, and then whose projects, line up across the gap.
            Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.xxl),
                ) {
                    webNative(Modifier.weight(1f).fillMaxHeight())
                    compose(Modifier.weight(1f).fillMaxHeight())
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.xxl),
                ) {
                    woolOnline(Modifier.weight(1f).fillMaxHeight())
                    composeProjects(Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun ToolkitPanel(
    label: String,
    title: String,
    chips: List<String>,
    points: List<String>,
    modifier: Modifier = Modifier,
    emphasised: Boolean = false,
) {
    val colors = BackAgainTheme.colors
    val typography = BackAgainTheme.slideTypography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, BackAgainShapes.medium)
            .border(if (emphasised) 2.dp else 1.dp, if (emphasised) colors.accent else colors.outline, BackAgainShapes.medium)
            .padding(BackAgainSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
    ) {
        Row { Pill(text = label, emphasised = emphasised) }
        Text(highlighted(title), style = typography.body, color = colors.onSurface)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
        ) {
            chips.forEach { Pill(it) }
        }
        Column {
            points.forEach { point ->
                Text("· $point", style = typography.caption, color = colors.onSurfaceVariant)
            }
        }
    }
}

/** One of my projects: its logo, its name, and where it's up to. */
@Composable
private fun ProjectCard(
    name: String,
    detail: String,
    logo: ProjectLogo,
    modifier: Modifier = Modifier,
) {
    val colors = BackAgainTheme.colors
    Row(
        modifier = modifier
            .border(1.dp, colors.outline, BackAgainShapes.medium)
            .padding(BackAgainSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
    ) {
        Image(
            painter = painterResource(logo.image),
            contentDescription = "$name logo",
            modifier = Modifier
                .size(48.dp)
                .clip(BackAgainShapes.large)
                .background(logo.background)
                .padding(logo.inset),
        )
        Column {
            Text(name, style = BackAgainTheme.slideTypography.caption, color = colors.onSurface)
            Text(detail, style = BackAgainTheme.slideTypography.caption, color = colors.onSurfaceVariant)
        }
    }
}

/**
 * A project's logo, on a rounded tile of its own [background]: the colour the logo sits on in its
 * own branding, so a logo that doesn't fill the tile still looks like one piece.
 */
private enum class ProjectLogo(
    val image: DrawableResource,
    val background: Color,
    val inset: Dp,
) {
    WoolOnline(Res.drawable.wool_online, Color(0xFFF7F4EC), inset = 6.dp),
    Reglyph(Res.drawable.reglyph, Color(0xFFF6F0E9), inset = 0.dp),
    Leegaa(Res.drawable.leegaa, Color(0xFF649E32), inset = 0.dp),
}

@Composable
internal fun BrowserComposableSlide() {
    SlideFrame(title = "The poll is a *composable*") {
        CodeAndPoints(
            code = """
                @Composable
                fun PollOptions(poll: Poll, tally: PollTally) {
                    Column {
                        for (option in poll.options) {
                            «val votes = tally.votes[option.id] ?: 0»
                            Row(«verticalAlignment = Alignment.CenterVertically») {
                                Text(option.label«, Modifier.weight(0.5f)»)
                                Box(«Modifier.weight(0.42f).height(10.dp)») {
                                    «val share = votes / tally.total.toFloat()»
                                    Box(«Modifier.fillMaxWidth(share).height(10.dp)»)
                                }
                                Text("${'$'}votes"«, Modifier.weight(0.08f)»)
                            }
                        }
                    }
                }
            """.trimIndent(),
            points = listOf(
                "*Column*, *Row*, *Box*, *Text*" to "The building blocks",
                "Same code\n*any* platform" to "Android, iOS, desktop, web",
            ),
            codeWeight = 3.1f,
        )
    }
}

/** The tap, the call it makes, and then, a step on, what that call puts on the wire. */
@Composable
internal fun VoteClientSlide() {
    val step = LocalSlideStep.current
    // The Kotlin shrinks to make room for the request below it.
    val codeScale by animateFloatAsState(if (step >= 1) 0.65f else 1f)
    SlideFrame(title = "*Sending* the vote") {
        BesidePoints(
            points = listOf(
                "*Ktor* client" to "JVM, Android, iOS and the browser",
                "Any client can call it" to "curl, JavaScript, Swift…",
                "You can see it" to "In your network tab",
            ),
            firstStep = 0,
        ) {
            CodeBlock(
                code = """
                    // client · UI
                    PollOptionBar(
                        «option = option,»
                        «enabled = poll.accepts(option.id),»
                        onClick = { onVote(option.id) },
                    )
                    // client · data
                    val castVote = CastVote { pollId, optionId ->
                        httpClient.post(PollApi.Votes(pollId)) {
                            setBody(PollApi.VoteRequest(optionId = optionId))
                        }
                    }
                """.trimIndent(),
                style = besidePointsCodeStyle(codeScale),
                focus = setOf(5, 9, 10),
            )
            Reveal(visible = step >= 1) {
                CodeBlock(
                    code = """
                        POST /api/polls/browser/votes
                        «Content-Type: application/json»
                        «X-Client-Id: 5c0f9e2a-…»

                        {"optionId":"safari"}
                    """.trimIndent(),
                    style = besidePointsCodeStyle(),
                    focus = setOf(5),
                )
            }
        }
    }
}

@Composable
internal fun ContractClassSlide() {
    SlideFrame(title = "*One* class, both sides") {
        BesidePoints(
            points = listOf(
                "In the *browser*" to "Encodes it, greys out IE",
                "On the *server*" to "Decodes it, same rule",
                "Written *once*" to "Compiled to Wasm and the JVM",
            ),
            firstStep = 0,
        ) {
            CodeBlock(
                code = """
                    // :feature:live:api, commonMain
                    @Serializable
                    data class VoteRequest(val optionId: String)

                    @Serializable
                    data class Poll(
                        val id: String,
                        val question: String,
                        val options: List<Option>,
                    ) {
                        fun accepts(optionId: String): Boolean =
                            options.any { it.id == optionId && it.enabled }
                    }
                """.trimIndent(),
                style = besidePointsCodeStyle(),
                focus = setOf(3, 11, 12),
            )
            // The option that rule turns away, as the phones showed it.
            PollOptions(
                poll = Polls.Browser.copy(options = Polls.Browser.options.filterNot { it.enabled }),
                tally = null,
                myVote = null,
            )
        }
    }
}

@Composable
internal fun ContractRefactorSlide() {
    val step = LocalSlideStep.current
    SlideFrame(title = "Rename it, and *both* sides break") {
        DiffBlock(
            removed = "data class VoteRequest(val optionId: String)",
            added = "data class VoteRequest(val choiceId: String)",
        )
        Reveal(visible = step >= 1) {
            ProblemsPanel(
                problems = listOf(
                    Triple("Unresolved reference 'optionId'.", "PollRoutes.kt", ":feature:live:server"),
                    Triple("No parameter with name 'optionId' found.", "AudienceRepository.kt", ":feature:live:client"),
                ),
            )
        }
        Reveal(visible = step >= 2) {
            Text(
                text = "Without shared code: keep both in sync by hand, or generate one with OpenAPI.",
                style = BackAgainTheme.slideTypography.caption,
                color = BackAgainTheme.colors.onSurfaceVariant,
            )
        }
    }
}

/** A one-line change as the editor's diff shows it: the old line on red, the new one on green. */
@Composable
private fun DiffBlock(
    removed: String,
    added: String,
) {
    val colors = BackAgainTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.codeBlock, BackAgainShapes.medium)
            .padding(vertical = BackAgainSpacing.md),
    ) {
        listOf("-" to removed, "+" to added).forEach { (sign, line) ->
            val tint = if (sign == "-") colors.error else colors.live
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tint.copy(alpha = 0.12f))
                    .padding(horizontal = BackAgainSpacing.md, vertical = BackAgainSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
            ) {
                Text(sign, style = BackAgainTheme.slideTypography.code, color = tint)
                Text(
                    text = rememberHighlightedCode(line, CodeLanguage.Kotlin),
                    style = BackAgainTheme.slideTypography.code,
                    softWrap = false,
                )
            }
        }
    }
}

/** The IDE's Problems tool window: each error, and the file and module it's in. */
@Composable
private fun ProblemsPanel(problems: List<Triple<String, String, String>>) {
    val colors = BackAgainTheme.colors
    val typography = BackAgainTheme.slideTypography
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, BackAgainShapes.medium)
            .border(1.dp, colors.outline, BackAgainShapes.medium)
            .padding(BackAgainSpacing.md),
        verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
    ) {
        Text("Problems   ${problems.size} errors", style = typography.caption, color = colors.onSurfaceVariant)
        val tall = LocalSlideFormat.current == SlideFormat.Tall
        problems.forEach { (message, file, module) ->
            val where = @Composable {
                Text(file, style = typography.caption, color = colors.onSurfaceVariant)
                Pill(module)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
            ) {
                Text("✕", style = typography.caption, color = colors.error)
                Text(message, style = typography.caption, color = colors.onSurface, modifier = Modifier.weight(1f))
                if (!tall) where()
            }
            if (tall) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
                ) {
                    where()
                }
            }
        }
    }
}

@Composable
internal fun ServerRouteSlide() {
    SlideFrame(title = "*Receiving* the vote") {
        CodeAndPoints(
            code = """
                // server · route
                post<PollApi.Votes> { votes ->
                    val request = call.receive<PollApi.VoteRequest>()
                    castVote(call.clientId(), votes.pollId, request.optionId)
                    «call.respond(HttpStatusCode.NoContent)»
                }
                // server · domain
                «val poll = Polls.all.firstOrNull { it.id == pollId }»
                require(poll != null && poll.accepts(optionId)) {
                    «"That isn't one of the options"»
                }
                recordVote(clientId, pollId, optionId)
            """.trimIndent(),
            points = listOf(
                "Same rule, *again*" to "Break the rule: server rejects",
                "*One* vote per participant" to "",
            ),
            firstStep = 0,
            focus = setOf(3, 9),
        )
    }
}

@Composable
internal fun ServerStoreSlide() {
    SlideFrame(title = "Saving to *Postgres*") {
        CodeAndPoints(
            code = """
                -- V1__live.sql
                CREATE TABLE poll_votes
                (
                    poll_id   text        «NOT NULL»,
                    client_id text        «NOT NULL»,
                    option_id text        «NOT NULL»,
                    «voted_at  timestamptz NOT NULL DEFAULT now(),»
                    PRIMARY KEY (poll_id, client_id)
                );
                -- R__notify_triggers.sql, for every row that changes
                PERFORM pg_notify('poll_votes',
                    «COALESCE(NEW.poll_id, OLD.poll_id)»);
            """.trimIndent(),
            language = CodeLanguage.Sql,
            points = listOf(
                "*One* row per participant" to "Voting again upserts",
                "*NOTIFY*: something changed" to "Remember this line",
            ),
            firstStep = 0,
            focus = setOf(8, 11),
        )
    }
}

@Composable
internal fun ListenNotifySlide() {
    val step = LocalSlideStep.current
    SlideFrame(title = "Postgres *LISTEN* / *NOTIFY*") {
        if (LocalSlideFormat.current == SlideFormat.Tall) {
            DiagramNode(title = "Postgres", detail = "vote saved, NOTIFY sent", modifier = Modifier.fillMaxWidth())
            Text("↓ NOTIFY poll_votes", style = BackAgainTheme.slideTypography.caption, color = BackAgainTheme.codeColors.keyword)
            DiagramNode(title = "Server", detail = "LISTENing", emphasised = true, modifier = Modifier.fillMaxWidth())
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DiagramNode(title = "Postgres", detail = "vote saved, NOTIFY sent", modifier = Modifier.width(380.dp))
                NotifyBus(scaledOut = step >= 1)
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(BusGap),
                ) {
                    ScaleOutNode(title = "Server 2", visible = step >= 1, modifier = Modifier.weight(1f).fillMaxHeight())
                    DiagramNode(title = "Server", detail = "LISTENing", emphasised = true, modifier = Modifier.weight(1f).fillMaxHeight())
                    ScaleOutNode(title = "Server 3", visible = step >= 1, modifier = Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
        Reveal(visible = step >= 1) {
            Text(
                text = highlighted("Scale out, and *every* server hears every vote."),
                style = BackAgainTheme.slideTypography.caption,
                color = BackAgainTheme.colors.onSurfaceVariant,
            )
        }
    }
}

/** The drop from Postgres to the servers: solid to the one that runs today, dashed to the ones scaling out would add. */
@Composable
private fun NotifyBus(scaledOut: Boolean) {
    val color = BackAgainTheme.codeColors.keyword
    val faint = BackAgainTheme.colors.onSurfaceVariant.copy(alpha = if (scaledOut) 0.6f else 0f)
    Box(modifier = Modifier.fillMaxWidth().height(96.dp)) {
        Canvas(Modifier.fillMaxWidth().height(96.dp)) {
            val width = 3.dp.toPx()
            val column = (size.width - BusGap.toPx() * 2) / 3
            val middle = size.width / 2
            val bus = size.height / 2
            drawLine(color, Offset(middle, 0f), Offset(middle, size.height), strokeWidth = width)
            val dashes = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
            listOf(column / 2, size.width - column / 2).forEach { x ->
                drawLine(faint, Offset(middle, bus), Offset(x, bus), strokeWidth = width, pathEffect = dashes)
                drawLine(faint, Offset(x, bus), Offset(x, size.height), strokeWidth = width, pathEffect = dashes)
            }
        }
        Text(
            text = "NOTIFY poll_votes",
            style = BackAgainTheme.slideTypography.caption,
            color = color,
            // Beside the drop, above the bus.
            modifier = Modifier.align(Alignment.TopCenter).offset(x = 130.dp, y = 4.dp),
        )
    }
}

@Composable
private fun ScaleOutNode(
    title: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = BackAgainTheme.colors
    Column(
        modifier = modifier
            .alpha(if (visible) 0.6f else 0f)
            .drawBehind {
                drawRoundRect(
                    color = colors.onSurfaceVariant,
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
                )
            }
            .padding(BackAgainSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
    ) {
        Text(title, style = BackAgainTheme.slideTypography.body, color = colors.onSurface)
        Text("if you scale out", style = BackAgainTheme.slideTypography.caption, color = colors.onSurfaceVariant)
    }
}

private val BusGap = 48.dp

@Composable
internal fun FlowServerSlide() {
    SlideFrame(title = "From NOTIFY to a *Flow*") {
        CodeBlock(
            code = """
                // server · data
                «val flowOfPollTallies = FlowOfPollTallies {»
                    storage.observeChangedPolls().mapNotNull { pollId -> tallyOf(pollId) }
                «}»
                // server · domain
                flowOfPollTallies().collect { tally ->
                    publishLiveEvent(LiveEvent.PollTallied(tally))
                }
                // server · services
                webSocket(LiveApi.SOCKET) {
                    flowOfLiveFrames().collect { frame -> sendSerialized(frame) }
                }
            """.trimIndent(),
            focus = setOf(3, 7, 11),
        )
        Text(
            text = highlighted("*One* recount per vote, *one* frame to every socket."),
            style = BackAgainTheme.slideTypography.caption,
            color = BackAgainTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
internal fun SocketSlide() {
    SlideFrame(title = "A *one-way* WebSocket") {
        CodeAndPoints(
            code = """
                ↓ {"seq":0,"event":{"type":"LiveEvent.Snapshot",…}}
                ↓ {"seq":41,"event":{"type":"LiveEvent.PollTallied",
                    "tally":{"pollId":"browser",
                             "votes":{"chrome":9,"safari":7}}}}
                ↓ {"seq":42,"event":{"type":"LiveEvent.ReactionsBurst",…}}
            """.trimIndent(),
            points = listOf(
                "A *snapshot* first" to "Then numbered changes",
                "Writes are *POSTs*" to "SSE would work too",
            ),
            firstStep = 0,
            codeWeight = 1.7f,
        )
    }
}

@Composable
internal fun FlowClientSlide() {
    SlideFrame(title = "A *Flow*, a *Flow*, a *Flow*…") {
        CodeAndPoints(
            code = """
                // client · data
                httpClient.webSocket(«endpoint.socketOrigin + »LiveApi.SOCKET) {
                    «while (true) {»
                        val frame = receiveDeserialized<LiveFrame>()
                        «state = state.applying(frame.event)»
                        send(state)
                    «}»
                }
                // client · ViewModel
                flowOfLiveState().collect { live ->
                    state.update { copy(live = live) }
                }
                // client · UI
                val state by viewModel.state.collectAsState()
            """.trimIndent(),
            focus = setOf(4, 6, 11, 14),
            points = listOf(
                "*WebSocket* frames" to "",
                "→ a *Flow*" to "",
                "→ Compose *state*" to "",
                "→ UI *redraws*" to "",
            ),
            codeWeight = 2.6f,
        )
    }
}
