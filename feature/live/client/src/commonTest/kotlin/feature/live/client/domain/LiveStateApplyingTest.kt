package feature.live.client.domain

import feature.live.DeckPosition
import feature.live.LiveEvent
import feature.live.PollTally
import feature.live.Question
import feature.live.Reaction
import feature.live.SystemStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class LiveStateApplyingTest {

    private val system = SystemStatus(
        revision = "backagain-00001",
        gitSha = "abc123",
        buildTime = "2026-09-23T00:00:00Z",
        startedAt = Instant.fromEpochSeconds(0),
    )

    private val snapshot = LiveEvent.Snapshot(
        position = DeckPosition(slideId = "title", step = 0),
        isLive = true,
        questions = listOf(question("1")),
        tallies = listOf(PollTally(pollId = "hosting", votes = mapOf("vms" to 2))),
        reactionTotals = mapOf(Reaction.Applause to 3L),
        viewers = 12,
        system = system,
    )

    @Test
    fun `a snapshot replaces everything and marks the connection live`() {
        val state = LiveState.Initial
            .copy(connection = LiveConnection.Reconnecting, viewers = 99)
            .applying(snapshot)

        assertEquals(LiveConnection.Connected, state.connection)
        assertEquals(12, state.viewers)
        assertEquals(DeckPosition("title", 0), state.position)
        assertEquals(system, state.system)
    }

    @Test
    fun `an asked question goes to the top once`() {
        val asked = question("2")
        val state = LiveState.Initial
            .applying(snapshot)
            .applying(LiveEvent.QuestionAsked(asked))
            .applying(LiveEvent.QuestionAsked(asked))

        assertEquals(listOf("2", "1"), state.questions.map { it.id })
    }

    @Test
    fun `a hidden question leaves the screen`() {
        val state = LiveState.Initial
            .applying(snapshot)
            .applying(LiveEvent.QuestionHidden("1"))

        assertEquals(emptyList(), state.questions)
    }

    @Test
    fun `a tally replaces only its own poll`() {
        val tally = PollTally(pollId = "wasm", votes = mapOf("soon" to 1))
        val state = LiveState.Initial
            .applying(snapshot)
            .applying(LiveEvent.PollTallied(tally))

        assertEquals(setOf("hosting", "wasm"), state.tallies.keys)
        assertEquals(tally, state.tallies["wasm"])
    }

    @Test
    fun `a reaction burst takes the server's totals`() {
        val state = LiveState.Initial
            .applying(snapshot)
            .applying(LiveEvent.ReactionsBurst(counts = mapOf(Reaction.Rocket to 2), totals = mapOf(Reaction.Applause to 3L, Reaction.Rocket to 2L)))

        assertEquals(mapOf(Reaction.Applause to 3L, Reaction.Rocket to 2L), state.reactionTotals)
    }

    private fun question(id: String) = Question(id = id, text = "question $id", askedAt = Instant.fromEpochSeconds(0))
}
