package feature.live

import feature.live.server.data.AudienceRepository
import feature.live.server.data.DeckStateRepository
import feature.live.server.data.LiveEventRepository
import feature.live.server.data.PollRepository
import feature.live.server.data.PresenterConfig
import feature.live.server.data.PresenterSessionRepository
import feature.live.server.data.QuestionRepository
import feature.live.server.data.ReactionRepository
import feature.live.server.data.RequestCountRepository
import feature.live.server.data.SystemStatusConfig
import feature.live.server.data.SystemStatusRepository
import feature.live.server.data.storage.DeckStateStorage
import feature.live.server.data.storage.PollVoteStorage
import feature.live.server.data.storage.PresenterSessionStorage
import feature.live.server.data.storage.QuestionStorage
import feature.live.server.data.storage.ReactionTotalStorage
import feature.live.server.domain.AddQuestion
import feature.live.server.domain.AddReactions
import feature.live.server.domain.AskQuestion
import feature.live.server.domain.AskQuestionImpl
import feature.live.server.domain.CastVote
import feature.live.server.domain.CastVoteImpl
import feature.live.server.domain.ClearAudience
import feature.live.server.domain.CountRequestsServed
import feature.live.server.domain.CountViewers
import feature.live.server.domain.FlowOfLiveFrames
import feature.live.server.domain.FlowOfLiveFramesImpl
import feature.live.server.domain.FlowOfPollTallies
import feature.live.server.domain.FlowOfPublishedFrames
import feature.live.server.domain.GetDeckState
import feature.live.server.domain.GetLiveSnapshot
import feature.live.server.domain.GetLiveSnapshotImpl
import feature.live.server.domain.GetPollTallies
import feature.live.server.domain.GetQuestionsOnScreen
import feature.live.server.domain.GetReactionTotals
import feature.live.server.domain.GetSystemStatus
import feature.live.server.domain.HideQuestion
import feature.live.server.domain.HideQuestionImpl
import feature.live.server.domain.IsPresenter
import feature.live.server.domain.MarkQuestionHidden
import feature.live.server.domain.PublishLiveCounters
import feature.live.server.domain.PublishLiveCountersImpl
import feature.live.server.domain.PublishLiveEvent
import feature.live.server.domain.PublishPollTallies
import feature.live.server.domain.PublishPollTalliesImpl
import feature.live.server.domain.RecordVote
import feature.live.server.domain.ResetAudience
import feature.live.server.domain.ResetAudienceImpl
import feature.live.server.domain.RestartRequestCount
import feature.live.server.domain.SendReaction
import feature.live.server.domain.SignInPresenter
import feature.live.server.domain.SignOutPresenter
import feature.live.server.domain.TakeQueuedReactions
import feature.live.server.domain.UpdateDeckState
import feature.live.server.domain.UpdatePresentation
import feature.live.server.domain.UpdatePresentationImpl
import feature.live.server.services.LiveCountersJob
import feature.live.server.services.LiveRoutes
import feature.live.server.services.PollRoutes
import feature.live.server.services.PollTalliesJob
import feature.live.server.services.PresenterRoutes
import feature.live.server.services.QuestionRoutes
import feature.live.server.services.ReactionRoutes
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.server.http.BackgroundJob
import platform.server.http.RouteHandler

val liveServerDependencies = module {
    single {
        PresenterConfig(
            password = System.getenv("BACKAGAIN_PRESENTER_PASSWORD")?.takeIf { it.isNotBlank() }
                ?: error("BACKAGAIN_PRESENTER_PASSWORD is not set"),
        )
    }
    single {
        SystemStatusConfig(
            revision = System.getenv("K_REVISION") ?: "local",
            gitSha = System.getenv("BACKAGAIN_GIT_SHA") ?: "local",
            buildTime = System.getenv("BACKAGAIN_BUILD_TIME") ?: "local",
        )
    }

    singleOf(::PresenterSessionStorage)
    singleOf(::DeckStateStorage)
    singleOf(::QuestionStorage)
    singleOf(::PollVoteStorage)
    singleOf(::ReactionTotalStorage)

    singleOf(::PresenterSessionRepository)
    single<SignInPresenter> { get<PresenterSessionRepository>().signInPresenter }
    single<SignOutPresenter> { get<PresenterSessionRepository>().signOutPresenter }
    single<IsPresenter> { get<PresenterSessionRepository>().isPresenter }

    singleOf(::DeckStateRepository)
    single<GetDeckState> { get<DeckStateRepository>().getDeckState }
    single<UpdateDeckState> { get<DeckStateRepository>().updateDeckState }

    singleOf(::QuestionRepository)
    single<AddQuestion> { get<QuestionRepository>().addQuestion }
    single<GetQuestionsOnScreen> { get<QuestionRepository>().getQuestionsOnScreen }
    single<MarkQuestionHidden> { get<QuestionRepository>().markQuestionHidden }

    singleOf(::PollRepository)
    single<GetPollTallies> { get<PollRepository>().getPollTallies }
    single<RecordVote> { get<PollRepository>().recordVote }
    single<FlowOfPollTallies> { get<PollRepository>().flowOfPollTallies }

    singleOf(::ReactionRepository)
    single<GetReactionTotals> { get<ReactionRepository>().getReactionTotals }
    single<AddReactions> { get<ReactionRepository>().addReactions }

    singleOf(::AudienceRepository)
    single<ClearAudience> { get<AudienceRepository>().clearAudience }

    singleOf(::RequestCountRepository)
    single<CountRequestsServed> { get<RequestCountRepository>().countRequestsServed }
    single<RestartRequestCount> { get<RequestCountRepository>().restartRequestCount }

    singleOf(::SystemStatusRepository)
    single<GetSystemStatus> { get<SystemStatusRepository>().getSystemStatus }

    singleOf(::LiveEventRepository)
    single<PublishLiveEvent> { get<LiveEventRepository>().publishLiveEvent }
    single<FlowOfPublishedFrames> { get<LiveEventRepository>().flowOfPublishedFrames }
    single<CountViewers> { get<LiveEventRepository>().countViewers }
    single<SendReaction> { get<LiveEventRepository>().sendReaction }
    single<TakeQueuedReactions> { get<LiveEventRepository>().takeQueuedReactions }

    singleOf(::UpdatePresentationImpl) bind UpdatePresentation::class
    singleOf(::AskQuestionImpl) bind AskQuestion::class
    singleOf(::HideQuestionImpl) bind HideQuestion::class
    singleOf(::CastVoteImpl) bind CastVote::class
    singleOf(::GetLiveSnapshotImpl) bind GetLiveSnapshot::class
    singleOf(::FlowOfLiveFramesImpl) bind FlowOfLiveFrames::class
    singleOf(::ResetAudienceImpl) bind ResetAudience::class
    singleOf(::PublishLiveCountersImpl) bind PublishLiveCounters::class
    singleOf(::PublishPollTalliesImpl) bind PublishPollTallies::class

    singleOf(::PresenterRoutes) bind RouteHandler::class
    singleOf(::QuestionRoutes) bind RouteHandler::class
    singleOf(::PollRoutes) bind RouteHandler::class
    singleOf(::ReactionRoutes) bind RouteHandler::class
    singleOf(::LiveRoutes) bind RouteHandler::class
    singleOf(::LiveCountersJob) bind BackgroundJob::class
    singleOf(::PollTalliesJob) bind BackgroundJob::class
}
