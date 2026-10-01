package platform.server.http

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TestTimeSource

class BandwidthBudgetTest {

    private val time = TestTimeSource()
    private val budget = BandwidthBudget(
        limits = listOf(
            BandwidthBudget.Limit(bytes = 100, window = 1.hours),
            BandwidthBudget.Limit(bytes = 250, window = 1.days),
        ),
        timeSource = time,
    )

    @Test
    fun `the spend that reaches a limit reports it, once`() {
        assertFalse(budget.spend(60))
        assertTrue(budget.spend(40))
        assertTrue(budget.isExhausted())
        assertFalse(budget.spend(10))
    }

    @Test
    fun `a window resets once it has passed`() {
        budget.spend(100)
        time += 59.minutes
        assertTrue(budget.isExhausted())

        time += 1.minutes
        assertFalse(budget.isExhausted())
    }

    @Test
    fun `the longer window still holds when the shorter one resets`() {
        budget.spend(100)
        time += 1.hours
        budget.spend(100)
        time += 1.hours
        budget.spend(50)
        time += 1.hours

        assertTrue(budget.isExhausted())
    }
}
