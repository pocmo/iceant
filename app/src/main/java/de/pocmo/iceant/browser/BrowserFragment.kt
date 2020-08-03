package de.pocmo.iceant.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import de.pocmo.iceant.R
import de.pocmo.iceant.browser.toolbar.ToolbarIntegration
import de.pocmo.iceant.downloads.DownloadService
import mozilla.components.browser.search.SearchEngineManager
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.usecases.EngineSessionUseCases
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.tabstray.BrowserTabsTray
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.findinpage.FindInPageFeature
import mozilla.components.feature.findinpage.view.FindInPageBar
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.feature.tabs.tabstray.TabsFeature
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import javax.inject.Inject

@AndroidEntryPoint
class BrowserFragment : Fragment() {
    @Inject lateinit var store: BrowserStore
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var searchEngineManager: SearchEngineManager

    @Inject lateinit var sessionUseCases: SessionUseCases
    @Inject lateinit var engineUseCases: EngineSessionUseCases
    @Inject lateinit var downloadUseCases: DownloadsUseCases
    @Inject lateinit var searchUseCases: SearchUseCases
    @Inject lateinit var tabsUseCases: TabsUseCases

    private val sessionFeature = ViewBoundFeatureWrapper<SessionFeature>()
    private val downloadsFeature = ViewBoundFeatureWrapper<DownloadsFeature>()
    private val findInPageFeature = ViewBoundFeatureWrapper<FindInPageFeature>()
    private val tabsFeature = ViewBoundFeatureWrapper<TabsFeature>()
    private val toolbarIntegration = ViewBoundFeatureWrapper<ToolbarIntegration>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionFeature.set(SessionFeature(
            store,
            sessionUseCases.goBack,
            engineUseCases,
            view.findViewById<View>(R.id.engineView) as EngineView
        ), this, view)

        val toolbar = view.findViewById<BrowserToolbar>(R.id.toolbar)
        val drawer = view.findViewById<DrawerLayout>(R.id.drawer)

        findInPageFeature.set(FindInPageFeature(
            store,
            view.findViewById<FindInPageBar>(R.id.findInPageBar),
            view.findViewById<View>(R.id.engineView) as EngineView
        ) {
            view.findViewById<FindInPageBar>(R.id.findInPageBar).visibility = View.GONE
        }, this, view)

        toolbarIntegration.set(ToolbarIntegration(
            requireContext(),
            sessionManager,
            store,
            toolbar,
            drawer,
            sessionUseCases,
            searchUseCases
        ) {
            view.findViewById<FindInPageBar>(R.id.findInPageBar).visibility = View.VISIBLE
            findInPageFeature.get()?.bind(store.state.selectedTab!!)
        }, this, view)

        downloadsFeature.set(DownloadsFeature(
            requireContext(),
            store,
            downloadUseCases,
            onNeedToRequestPermissions = { permissions ->
                requestPermissions(permissions, 1)
            },
            downloadManager = FetchDownloadManager(
                requireContext(),
                store,
                DownloadService::class
            ),
            fragmentManager = childFragmentManager
        ), this, view)

        tabsFeature.set(TabsFeature(
            view.findViewById<BrowserTabsTray>(R.id.tabsTray),
            store,
            tabsUseCases.selectTab,
            tabsUseCases.removeTab
        ) {
            drawer.close()
        }, this, view)

        view.findViewById<Button>(R.id.addTab).setOnClickListener {
            tabsUseCases.addTab("about:blank", selectTab = true)
            drawer.close()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        downloadsFeature.get()?.onPermissionsResult(permissions, grantResults)
    }
}
