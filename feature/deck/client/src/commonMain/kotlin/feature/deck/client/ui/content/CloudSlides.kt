package feature.deck.client.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import feature.deck.client.ui.Cards
import feature.deck.client.ui.Columns
import feature.deck.client.ui.LocalSlideFormat
import feature.deck.client.ui.NumberedList
import feature.deck.client.ui.Pill
import feature.deck.client.ui.SlideFormat
import feature.deck.client.ui.SlideFrame
import feature.deck.client.ui.Stat
import feature.deck.client.ui.highlighted
import feature.live.client.domain.LiveState
import platform.design.BackAgainShapes
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme

@Composable
internal fun GoogleCloudMapSlide() {
    SlideFrame(title = "On *Google Cloud*") {
        RoundTripMap(stop = 3, onGoogleCloud = true)
    }
}

@Composable
internal fun ShipStaticSlide() {
    SlideFrame(
        title = "Serving the *Wasm*",
        headerEnd = { ZoomBreadcrumb(current = 0) },
    ) {
        Columns(
            start = {
                HostingCard(
                    who = "This deck",
                    how = "Served by the *Kotlin server*",
                    detail = "Same container as the backend",
                    emphasised = true,
                )
            },
            end = {
                HostingCard(
                    who = "Alternative",
                    how = "*Cloud Storage* and a *CDN*",
                    detail = "Cached at the edge",
                )
            },
        )
    }
}

@Composable
private fun HostingCard(
    who: String,
    how: String,
    detail: String,
    emphasised: Boolean = false,
) {
    val colors = BackAgainTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, BackAgainShapes.medium)
            .border(if (emphasised) 2.dp else 1.dp, if (emphasised) colors.accent else colors.outline, BackAgainShapes.medium)
            .padding(BackAgainSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
    ) {
        Row { Pill(text = who, emphasised = emphasised) }
        Text(highlighted(how), style = BackAgainTheme.slideTypography.body, color = colors.onSurface)
        Text(detail, style = BackAgainTheme.slideTypography.caption, color = colors.onSurfaceVariant)
    }
}

@Composable
internal fun CloudRunSlide(live: LiveState) {
    SlideFrame(
        title = "The backend: *Cloud Run*",
        headerEnd = { ZoomBreadcrumb(current = 1) },
    ) {
        Cards(
            cards = listOf(
                "A *container*" to "The Kotlin server, Wasm included",
                "Scales *horizontally*" to "More instances with traffic, zero when idle",
                "Scales *vertically*" to "1 CPU, 1 GiB today; bigger for heavier apps",
            ),
        )
        val stats = @Composable {
            Stat(value = live.viewers.toString(), label = "open sockets: that's you")
            Stat(value = live.requestsServed.toString(), label = "requests since we went live")
        }
        if (LocalSlideFormat.current == SlideFormat.Tall) {
            Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.lg)) { stats() }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.xxl)) { stats() }
        }
    }
}

@Composable
internal fun CloudSqlSlide() {
    SlideFrame(
        title = "Postgres: *Cloud SQL*",
        headerEnd = { ZoomBreadcrumb(current = 2) },
    ) {
        Cards(
            cards = listOf(
                "*Managed* Postgres" to "Backups, patches, upgrades",
                "*Private* to the backend" to "Via the Cloud SQL connector",
                "Scales *vertically*" to "db-f1-micro today; a bigger tier is one line",
            ),
        )
    }
}

@Composable
internal fun ShipPipelineSlide() {
    SlideFrame(title = "From *git push* to production") {
        NumberedList(
            items = listOf(
                "git push",
                "GitHub Actions builds the *Wasm* and the *JVM*",
                "*One* container image",
                "*Terraform* applies",
                "*Cloud Run* rolls out",
            ),
        )
    }
}
