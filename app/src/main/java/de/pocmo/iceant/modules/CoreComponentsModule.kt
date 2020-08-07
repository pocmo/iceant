package de.pocmo.iceant.modules

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import de.pocmo.iceant.downloads.DownloadService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
import mozilla.components.browser.search.SearchEngineManager
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.storage.AutoSave
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.thumbnails.ThumbnailsMiddleware
import mozilla.components.browser.thumbnails.storage.ThumbnailStorage
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.downloads.DownloadMiddleware
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
    fun provideStore(
        application: Application,
        thumbnailStorage: ThumbnailStorage
    ): BrowserStore {
        return BrowserStore(
            middleware = listOf(
                DownloadMiddleware(application.applicationContext, DownloadService::class.java),
                ThumbnailsMiddleware(thumbnailStorage)
            )
        )
    }

    @Provides
    @Singleton
    fun provideSessionStorage(application: Application, engine: Engine): SessionStorage {
        return SessionStorage(application, engine)
    }

    @Provides
    @Singleton
    fun providesSessionManager(
        engine: Engine,
        store: BrowserStore,
        sessionStorage: SessionStorage
    ): SessionManager {
        return SessionManager(engine, store).apply {
            sessionStorage.restore()?.let { restore(it) }

            AutoSave(this, sessionStorage, 5000)
                .whenSessionsChange()
                .whenGoingToBackground()
                .periodicallyInForeground()
        }
    }

    @Provides
    @Singleton
    fun providesClient(application: Application): Client {
        return GeckoViewFetchClient(application)
    }

    @Provides
    @Singleton
    fun providesSearchEngineManager(application: Application): SearchEngineManager {
        return SearchEngineManager().apply {
            GlobalScope.launch {
                loadAsync(application).await()
            }
        }
    }

    @Provides
    @Singleton
    fun providesThumbnailStorage(application: Application): ThumbnailStorage {
        return ThumbnailStorage(application)
    }
}
