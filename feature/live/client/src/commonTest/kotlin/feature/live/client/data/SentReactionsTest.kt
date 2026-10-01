package feature.live.client.data

import feature.live.Reaction
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SentReactionsTest {

    private val clock = object : Clock {
        var now = Instant.fromEpochSeconds(0)

        override fun now() = now
    }
    private val sentReactions = SentReactions(clock)

    @Test
    fun `a burst loses as many of each reaction as this device sent`() = runTest {
        sentReactions.sent(Reaction.Love)
        sentReactions.sent(Reaction.Love)
        sentReactions.sent(Reaction.Rocket)

        val burst = sentReactions.withoutOwn(mapOf(Reaction.Love to 3, Reaction.Rocket to 1, Reaction.Idea to 2))

        assertEquals(mapOf(Reaction.Love to 1, Reaction.Idea to 2), burst)
    }

    @Test
    fun `a sent reaction is only taken out of one burst`() = runTest {
        sentReactions.sent(Reaction.Love)

        sentReactions.withoutOwn(mapOf(Reaction.Love to 1))

        assertEquals(mapOf(Reaction.Love to 1), sentReactions.withoutOwn(mapOf(Reaction.Love to 1)))
    }

    @Test
    fun `a failed reaction is not waited for`() = runTest {
        sentReactions.sent(Reaction.Party)
        sentReactions.failed(Reaction.Party)

        assertEquals(mapOf(Reaction.Party to 1), sentReactions.withoutOwn(mapOf(Reaction.Party to 1)))
    }

    @Test
    fun `a reaction that never came back stops hiding others`() = runTest {
        sentReactions.sent(Reaction.Applause)
        clock.now += 6.seconds

        assertEquals(mapOf(Reaction.Applause to 1), sentReactions.withoutOwn(mapOf(Reaction.Applause to 1)))
    }
}
