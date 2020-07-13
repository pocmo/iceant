package de.pocmo.iceant.modules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.fetch.Client
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class CoreComponentsModule {
    @Provides
    @Singleton
    fun providesEngine(application: Application): Engine {
        return GeckoEngine(application)
    }

    @Provides
    @Singleton
    fun provideStore(): BrowserStore {
        return BrowserStore()
    }

    @Provides
    @Singleton
    fun providesSessionManager(engine: Engine, store: BrowserStore): SessionManager {
        return SessionManager(engine, store)
    }

    @Provides
    @Singleton
    fun providesClient(application: Application): Client {
        return GeckoViewFetchClient(application)
    }
}
