package feature.deck.client.ui.content

import androidx.compose.runtime.Composable
import feature.deck.client.ui.Cards
import feature.deck.client.ui.SlideFrame
import feature.deck.client.ui.TitleSlide

@Composable
internal fun VerdictSlide() {
    TitleSlide(
        title = "Would I *do it again*?",
    )
}

@Composable
internal fun VerdictAnswerSlide() {
    TitleSlide(
        title = "Would I do it again?\n*Yes!*",
        subtitle = "And if Compose/Wasm isn't a good fit,\nI'd still choose Kotlin.",
    )
}

@Composable
internal fun AdvantagesSlide() {
    SlideFrame(title = "*Advantages*") {
        Cards(
            cards = listOf(
                "*One* language" to "Browser to database",
                "Write *once*, run everywhere" to "Android, iOS, web, desktop",
                "*Compiler-checked* contracts" to "Change once, checked twice",
                "Compose skills *carry over*" to "Android devs are most of the way there",
                "Feels like an *app*" to "Not a web page",
            ),
            columns = 2,
        )
    }
}

@Composable
internal fun DisadvantagesSlide() {
    SlideFrame(title = "*Disadvantages*") {
        Cards(
            cards = listOf(
                "It's a *canvas*" to "Accessibility, SEO, find-in-page",
                "A *bigger* first download" to "3.7 MB with brotli",
                "*Slower* builds" to "Especially Wasm",
                "A *younger* ecosystem" to "Fewer web libraries",
            ),
            columns = 2,
        )
    }
}
