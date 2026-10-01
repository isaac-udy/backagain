package feature.live.client.domain

import feature.live.Polls
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PollAcceptsTest {

    @Test
    fun `a poll accepts its enabled options`() {
        assertTrue(Polls.Browser.accepts("safari"))
    }

    @Test
    fun `nobody gets to vote for Internet Explorer`() {
        assertFalse(Polls.Browser.accepts("ie"))
    }

    @Test
    fun `a poll doesn't accept an option it doesn't have`() {
        assertFalse(Polls.Browser.accepts("netscape"))
    }
}
