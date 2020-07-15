package de.pocmo.iceant.browser.toolbar

import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.toolbar.ToolbarFeature
import mozilla.components.support.base.feature.LifecycleAwareFeature

class ToolbarIntegration(
    store: BrowserStore,
    toolbar: BrowserToolbar,
    sessionUseCases: SessionUseCases,
    private val searchUseCases: SearchUseCases,
    private val findInPage: () -> Unit
) : LifecycleAwareFeature {
    private val toolbarFeature = ToolbarFeature(
        toolbar,
        store,
        sessionUseCases.loadUrl,
        { searchTerms -> searchUseCases.defaultSearch(searchTerms) }
    )

    init {
        toolbar.display.menuBuilder = BrowserMenuBuilder(
            items = listOf(
                SimpleBrowserMenuItem(
                    label = "Find in Page",
                    listener = { findInPage() }
                )
            )
        )
    }

    override fun start() {
        toolbarFeature.start()
    }

    override fun stop() {
        toolbarFeature.stop()
    }
}
