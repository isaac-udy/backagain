package feature.live

import feature.live.client.data.AudienceRepository
import feature.live.client.data.LiveSocketRepository
import feature.live.client.data.PresenterRepository
import feature.live.client.data.SentReactions
import feature.live.client.data.storage.PresenterTokenStorage
import feature.live.client.data.storage.VoteStorage
import feature.live.client.domain.AskQuestion
import feature.live.client.domain.CastVote
import feature.live.client.domain.FlowOfIsPresenter
import feature.live.client.domain.FlowOfLiveState
import feature.live.client.domain.FlowOfMyVotes
import feature.live.client.domain.FlowOfReactionBursts
import feature.live.client.domain.HideQuestion
import feature.live.client.domain.ResetAudience
import feature.live.client.domain.SendReaction
import feature.live.client.domain.SignInPresenter
import feature.live.client.domain.SignOutPresenter
import feature.live.client.domain.UpdatePresentation
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val liveClientDependencies = module {
    singleOf(::PresenterTokenStorage)
    singleOf(::VoteStorage)
    singleOf(::SentReactions)

    singleOf(::LiveSocketRepository)
    single<FlowOfLiveState> { get<LiveSocketRepository>().flowOfLiveState }
    single<FlowOfReactionBursts> { get<LiveSocketRepository>().flowOfReactionBursts }

    singleOf(::PresenterRepository)
    single<FlowOfIsPresenter> { get<PresenterRepository>().flowOfIsPresenter }
    single<SignInPresenter> { get<PresenterRepository>().signInPresenter }
    single<SignOutPresenter> { get<PresenterRepository>().signOutPresenter }
    single<UpdatePresentation> { get<PresenterRepository>().updatePresentation }
    single<HideQuestion> { get<PresenterRepository>().hideQuestion }
    single<ResetAudience> { get<PresenterRepository>().resetAudience }

    singleOf(::AudienceRepository)
    single<AskQuestion> { get<AudienceRepository>().askQuestion }
    single<CastVote> { get<AudienceRepository>().castVote }
    single<FlowOfMyVotes> { get<AudienceRepository>().flowOfMyVotes }
    single<SendReaction> { get<AudienceRepository>().sendReaction }
}
