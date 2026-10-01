package platform.client.http

import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

val httpClientDependencies: Module = module {
    single { ApiEndpoint(origin = currentOrigin()) }
    single { loadOrCreateClientId() }
    single<HttpClient> { ApiHttpClient(engine = platformHttpEngine(), endpoint = get(), clientId = get()) }
}
