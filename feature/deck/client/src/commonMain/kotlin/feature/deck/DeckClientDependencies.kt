package feature.deck

import feature.deck.client.data.AddressRepository
import feature.deck.client.data.storage.AddressBarStorage
import feature.deck.client.domain.FlowOfIsStage
import feature.deck.client.domain.FlowOfSlidesFromHistory
import feature.deck.client.domain.GetStartAddress
import feature.deck.client.domain.SetStage
import feature.deck.client.domain.ShowSlideAddress
import feature.deck.client.ui.ContentSlideViewModel
import feature.deck.client.ui.DeckSettingsViewModel
import feature.deck.client.ui.DeckViewModel
import feature.deck.client.ui.PollSlideViewModel
import feature.deck.client.ui.PresenterSignInViewModel
import feature.deck.client.ui.QuestionsSlideViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val deckClientDependencies = module {
    singleOf(::AddressBarStorage)
    singleOf(::AddressRepository)
    single<GetStartAddress> { get<AddressRepository>().getStartAddress }
    single<ShowSlideAddress> { get<AddressRepository>().showSlideAddress }
    single<FlowOfSlidesFromHistory> { get<AddressRepository>().flowOfSlidesFromHistory }
    single<FlowOfIsStage> { get<AddressRepository>().flowOfIsStage }
    single<SetStage> { get<AddressRepository>().setStage }

    viewModelOf(::DeckViewModel)
    viewModelOf(::ContentSlideViewModel)
    viewModelOf(::QuestionsSlideViewModel)
    viewModelOf(::PollSlideViewModel)
    viewModelOf(::PresenterSignInViewModel)
    viewModelOf(::DeckSettingsViewModel)
}
