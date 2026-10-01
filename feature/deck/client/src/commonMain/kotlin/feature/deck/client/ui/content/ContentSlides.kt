package feature.deck.client.ui.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import feature.deck.client.domain.Talk
import feature.deck.client.ui.Eyebrow
import feature.deck.client.ui.LocalSlideFormat
import feature.deck.client.ui.Pill
import feature.deck.client.ui.SlideFormat
import feature.deck.client.ui.SlideFrame
import feature.deck.client.ui.sharedAcrossSlides
import feature.live.client.domain.LiveState
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import platform.design.BackAgainShapes
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme

/**
 * The slides that are words, code and diagrams. The interactive ones have screens of their own.
 *
 * Slides carry prompts, not sentences: the talking happens out loud, and the detail lives in each
 * slide's notes in [Talk].
 */
@Composable
internal fun ContentSlide(
    slideId: String,
    live: LiveState,
) {
    when (slideId) {
        // Section 1: hook, join and who I am
        "cover" -> CoverSlide()
        "hook" -> HookSlide()
        "whoami" -> WhoAmISlide()

        // Section 2: Kotlin Multiplatform in three minutes
        "kmp-targets" -> KmpTargetsSlide()
        "kmp-how" -> KmpHowSlide()

        // Section 3: stop 1, the browser
        "map-browser" -> MapSlide(title = "Stop 1: the *browser*", stop = 1)
        "kmp-wasm" -> KmpWasmSlide()
        "browser-composable" -> BrowserComposableSlide()
        "vote-client" -> VoteClientSlide()

        // Section 4: stop 2, the contract
        "map-contract" -> MapSlide(title = "Stop 2: the *contract*", stop = 2)
        "contract-class" -> ContractClassSlide()
        "contract-refactor" -> ContractRefactorSlide()

        // Section 5: stop 3, the backend
        "map-backend" -> MapSlide(title = "Stop 3: the *backend*", stop = 3)
        "server-route" -> ServerRouteSlide()
        "server-store" -> ServerStoreSlide()

        // Section 6: stop 4, and back again; the reveal is a poll slide
        "map-back" -> MapSlide(title = "Stop 4: …and *back again*", stop = 4, returnLabel = "LISTEN / NOTIFY, then a WebSocket")
        "listen-notify" -> ListenNotifySlide()
        "flow-server" -> FlowServerSlide()
        "socket" -> SocketSlide()
        "flow-client" -> FlowClientSlide()

        // Section 7: on Google Cloud, then shipping it
        "map-gcp" -> GoogleCloudMapSlide()
        "ship-static" -> ShipStaticSlide()
        "cloudrun-what" -> CloudRunSlide(live = live)
        "cloudsql" -> CloudSqlSlide()
        "ship-pipeline" -> ShipPipelineSlide()

        // Section 8: the verdict and questions
        "verdict-worry" -> VerdictSlide()
        "verdict-advantages" -> AdvantagesSlide()
        "verdict-disadvantages" -> DisadvantagesSlide()
        "verdict-yes" -> VerdictAnswerSlide()

        else -> SlideFrame(title = slideId) {}
    }
}

/**
 * A slide's [content], below its title, with the code to join the deck beside it and level with its
 * top. Every slide from the hook to the opening poll has the same title height, so the code stays
 * where it is between them. A phone has already joined, and gets only [content].
 */
@Composable
internal fun ColumnScope.WithJoinCode(content: @Composable ColumnScope.() -> Unit) {
    if (LocalSlideFormat.current == SlideFormat.Tall) {
        content()
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.xxl),
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.xl),
            content = content,
        )
        JoinCode(size = 420.dp)
    }
}

/** The code to join the deck: one element across the slides that show it, moving between them. */
@Composable
internal fun JoinCode(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    QrCode(address = Talk.ADDRESS, size = size, modifier = modifier.sharedAcrossSlides("join-code"))
}

/** A QR code for [address], on the white it needs to scan, with what it's for above it if there's a [label]. */
@Composable
internal fun QrCode(
    address: String,
    size: Dp,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
    ) {
        if (label != null) Eyebrow(label)
        Box(
            modifier = Modifier
                .clip(BackAgainShapes.large)
                .background(Color.White)
                .padding(BackAgainSpacing.md),
        ) {
            Image(
                painter = rememberQrCodePainter("https://$address"),
                contentDescription = "QR code for $address",
                modifier = Modifier.size(size),
            )
        }
        // Sized to span the code exactly, on one line.
        Text(
            text = address,
            style = BackAgainTheme.slideTypography.caption,
            color = BackAgainTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = 96.sp),
            modifier = Modifier.width(size + BackAgainSpacing.md * 2),
        )
    }
}

/** Which of the map's three boxes section 7 has zoomed into: 0 the browser, 1 the backend, 2 Postgres. */
@Composable
internal fun ZoomBreadcrumb(current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm)) {
        listOf("Browser", "Backend", "Postgres").forEachIndexed { index, name ->
            Pill(text = name, emphasised = index == current)
        }
    }
}
