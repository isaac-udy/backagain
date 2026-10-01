package feature.live.client.data

import feature.live.client.domain.AskQuestion
import feature.live.client.data.storage.VoteStorage
import feature.live.client.domain.CastVote
import feature.live.client.domain.FlowOfMyVotes
import feature.live.client.domain.SendReaction
import feature.live.server.services.PollApi
import feature.live.server.services.QuestionApi
import feature.live.server.services.ReactionApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody

/**
 * What anyone in the audience can do. Each is a one-shot request; the effect comes back to every
 * client, this one included, over the live socket. A reaction also shows here straight away; see
 * [SentReactions].
 */
internal class AudienceRepository(
    private val httpClient: HttpClient,
    private val sentReactions: SentReactions,
    private val voteStorage: VoteStorage,
) {
    val askQuestion = AskQuestion { text ->
        httpClient.post(QuestionApi.Questions()) {
            setBody(QuestionApi.AskRequest(text))
        }
    }

    val castVote = CastVote { pollId, optionId ->
        httpClient.post(PollApi.Votes(pollId)) {
            setBody(PollApi.VoteRequest(optionId = optionId))
        }
        voteStorage.setVote(pollId, optionId)
    }

    val flowOfMyVotes = FlowOfMyVotes { voteStorage.votes }

    val sendReaction = SendReaction { reaction ->
        sentReactions.sent(reaction)
        try {
            httpClient.post(ReactionApi.Reactions()) {
                setBody(ReactionApi.ReactionRequest(reaction))
            }
        } catch (failure: Throwable) {
            sentReactions.failed(reaction)
            throw failure
        }
    }
}
