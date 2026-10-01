package feature.live.server.domain

import feature.live.PollTally

/** A tally for every poll in `feature.live.Polls`, including those nobody has voted in. */
fun interface GetPollTallies {
    suspend operator fun invoke(): List<PollTally>
}
