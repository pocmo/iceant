package de.pocmo.iceant

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import de.pocmo.iceant.downloads.DownloadService
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.search.SearchEngineManager
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.usecases.EngineSessionUseCases
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.findinpage.FindInPageFeature
import mozilla.components.feature.findinpage.view.FindInPageBar
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.toolbar.SearchUseCase
import mozilla.components.feature.toolbar.ToolbarFeature
import javax.inject.Inject

@AndroidEntryPoint
class BrowserActivity : AppCompatActivity() {
    @Inject lateinit var engine: Engine
    @Inject lateinit var store: BrowserStore
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var searchEngineManager: SearchEngineManager

    private lateinit var sessionFeature: SessionFeature
    private lateinit var toolbarFeature: ToolbarFeature
    private lateinit var downloadsFeature: DownloadsFeature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_browser)

        val sessionUseCases = SessionUseCases(sessionManager)
        val engineUseCases = EngineSessionUseCases(sessionManager)
        val downloadUseCases = DownloadsUseCases(store)
        val searchUseCases = SearchUseCases(this, searchEngineManager, sessionManager)

        sessionFeature = SessionFeature(
            store,
            sessionUseCases.goBack,
            engineUseCases,
            findViewById<View>(R.id.engineView) as EngineView
        )

        val toolbar = findViewById<BrowserToolbar>(R.id.toolbar)

        val findInPageFeature = FindInPageFeature(
            store,
            findViewById<FindInPageBar>(R.id.findInPageBar),
            findViewById<View>(R.id.engineView) as EngineView
        ) {
            findViewById<FindInPageBar>(R.id.findInPageBar).visibility = View.GONE
        }

        toolbar.display.menuBuilder = BrowserMenuBuilder(
            items = listOf(
                SimpleBrowserMenuItem(
                    label = "Find in Page",
                    listener = {
                        findViewById<FindInPageBar>(R.id.findInPageBar).visibility = View.VISIBLE
                        findInPageFeature.bind(store.state.selectedTab!!)
                    }
                )
            )
        )

        toolbarFeature = ToolbarFeature(
            toolbar,
            store,
            sessionUseCases.loadUrl,
            { searchTerms -> searchUseCases.defaultSearch(searchTerms) }
        )

        downloadsFeature = DownloadsFeature(
            applicationContext,
            store,
            downloadUseCases,
            onNeedToRequestPermissions = { permissions ->
                requestPermissions(permissions, 1)
            },
            downloadManager = FetchDownloadManager(
                applicationContext,
                store,
                DownloadService::class
            ),
            fragmentManager = supportFragmentManager
        )

        sessionManager.add(
            Session("https://www.mozilla.org")
        )

        lifecycle.addObserver(sessionFeature)
        lifecycle.addObserver(toolbarFeature)
        lifecycle.addObserver(downloadsFeature)
        lifecycle.addObserver(findInPageFeature)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        downloadsFeature.onPermissionsResult(permissions, grantResults)
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        if (name == EngineView::class.java.name) {
            return engine.createView(context, attrs).asView()
        }

        return super.onCreateView(name, context, attrs)
    }
}
