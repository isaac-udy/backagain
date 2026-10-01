package feature.deck.client.domain

import feature.live.Polls

/** The talk: every slide in order, with the presenter's notes. The visuals live in `client.ui.content`. */
object Talk {
    /** Where the audience joins; shown on the cover, hook and closing slides. */
    const val ADDRESS: String = "backagain.isaacudy.com"

    const val REPOSITORY: String = "github.com/isaac-udy/backagain"

    val deck: Deck = Deck(
        slides = listOf(
            // Section 1: hook, join and who I am (2:30)
            Slide(
                id = "cover",
                title = "Kotlin in Google Cloud",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "hook",
                title = "this is an application",
                kind = Slide.Kind.Content,
                growsFromPrevious = true,
            ),
            Slide(
                id = "whoami",
                title = "hi, I'm Isaac",
                kind = Slide.Kind.Content,
                growsFromPrevious = true,
            ),
            Slide(
                id = "poll-open",
                title = "Do you use Kotlin?",
                kind = Slide.Kind.Poll(Polls.UsesKotlin.id, showsJoinCode = true),
                growsFromPrevious = true,
            ),

            // Section 2: Kotlin Multiplatform in three minutes (3:00)
            Slide(
                id = "kmp-targets",
                title = "Kotlin has five targets",
                kind = Slide.Kind.Content,
                steps = 1,
            ),
            Slide(
                id = "kmp-how",
                title = "Shared code, compiled per target",
                kind = Slide.Kind.Content,
                steps = 2,
            ),

            // Section 3: stop 1, the browser, and the traced vote (4:30)
            Slide(
                id = "map-browser",
                title = "Stop 1: the browser",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "vote",
                title = "Which browser are you using?",
                kind = Slide.Kind.Poll(Polls.Browser.id, resultsFromStep = null),
            ),
            Slide(
                id = "kmp-wasm",
                title = "Two ways to draw a web UI",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "browser-composable",
                title = "The poll is a composable",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "vote-client",
                title = "Sending the vote",
                kind = Slide.Kind.Content,
                steps = 2,
            ),

            // Section 4: stop 2, the contract (3:30)
            Slide(
                id = "map-contract",
                title = "Stop 2: the contract",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "contract-class",
                title = "One class, both sides",
                kind = Slide.Kind.Content,
                steps = 2,
            ),
            Slide(
                id = "contract-refactor",
                title = "Rename it, and both sides break",
                kind = Slide.Kind.Content,
                steps = 2,
            ),

            // Section 5: stop 3, the backend (3:00)
            Slide(
                id = "map-backend",
                title = "Stop 3: the backend",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "server-route",
                title = "Receiving the vote",
                kind = Slide.Kind.Content,
                steps = 1,
            ),
            Slide(
                id = "server-store",
                title = "Saving to Postgres",
                kind = Slide.Kind.Content,
                steps = 1,
            ),

            // Section 6: stop 4, and back again, ending in the results reveal (3:00)
            Slide(
                id = "map-back",
                title = "Stop 4: …and back again",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "listen-notify",
                title = "Postgres LISTEN / NOTIFY",
                kind = Slide.Kind.Content,
                steps = 1,
            ),
            Slide(
                id = "flow-server",
                title = "From NOTIFY to a Flow",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "socket",
                title = "A one-way WebSocket",
                kind = Slide.Kind.Content,
                steps = 1,
            ),
            Slide(
                id = "flow-client",
                title = "A Flow, a Flow, a Flow…",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "reveal",
                title = "Which browser are you using?",
                kind = Slide.Kind.Poll(Polls.Browser.id, resultsFromStep = 1),
                steps = 1,
            ),

            // Section 7: on Google Cloud, then shipping it (4:30)
            Slide(
                id = "map-gcp",
                title = "On Google Cloud",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "ship-static",
                title = "Serving the Wasm",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "cloudrun-what",
                title = "The backend: Cloud Run",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "cloudsql",
                title = "Postgres: Cloud SQL",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "ship-pipeline",
                title = "From git push to production",
                kind = Slide.Kind.Content,
                steps = 4,
            ),

            // Section 8: the verdict and questions (3:00)
            Slide(
                id = "verdict-worry",
                title = "Would I do it again?",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "verdict-advantages",
                title = "Advantages",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "verdict-disadvantages",
                title = "Disadvantages",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "verdict-yes",
                title = "Would I do it again? Yes!",
                kind = Slide.Kind.Content,
            ),
            Slide(
                id = "thanks",
                title = "Questions?",
                kind = Slide.Kind.Questions,
            ),
        ),
    )
}
