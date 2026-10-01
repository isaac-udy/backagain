package feature.live

/** The polls the deck asks. Both sides read them from here, so a vote names an option that exists. */
object Polls {
    val UsesKotlin: Poll = Poll(
        id = "uses-kotlin",
        question = "Do you use Kotlin?",
        options = listOf(
            Poll.Option(id = "yes", label = "Yes"),
            Poll.Option(id = "no", label = "No"),
            Poll.Option(id = "sometimes", label = "Sometimes"),
            Poll.Option(id = "what", label = "What's Kotlin?"),
        ),
    )

    val Browser: Poll = Poll(
        id = "browser",
        question = "Which browser are you using?",
        options = listOf(
            Poll.Option(id = "chrome", label = "Chrome"),
            Poll.Option(id = "safari", label = "Safari"),
            Poll.Option(id = "other", label = "Other"),
            Poll.Option(id = "ie", label = "Internet Explorer", enabled = false),
        ),
    )

    val all: List<Poll> = listOf(UsesKotlin, Browser)
}
