package feature.live.server.domain

import feature.live.PollTally
import kotlinx.coroutines.flow.Flow

/** A poll's fresh tally every time its votes change, from whichever server changed them. */
fun interface FlowOfPollTallies {
    operator fun invoke(): Flow<PollTally>
}
