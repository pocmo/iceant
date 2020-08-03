package de.pocmo.iceant.browser.toolbar

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import de.pocmo.iceant.R
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.feature.toolbar.ToolbarFeature
import mozilla.components.support.base.feature.LifecycleAwareFeature

class ToolbarIntegration(
    context: Context,
    sessionManager: SessionManager,
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

    private val tabsToolbarFeature = TabsToolbarFeature(
        toolbar,
        sessionManager
    ) {
        // TODO
    }

    init {
        toolbar.display.menuBuilder = BrowserMenuBuilder(
            items = listOf(
                SimpleBrowserMenuItem(
                    label = "Find in Page",
                    listener = { findInPage() }
                )
            )
        )

        toolbar.display.setUrlBackground(
            AppCompatResources.getDrawable(context, R.drawable.url_background)
        )

        toolbar.display.colors = toolbar.display.colors.copy(
            text = 0xFFFFFFFF.toInt()
        )

        toolbar.edit.colors = toolbar.edit.colors.copy(
            text = 0xFFFFFFFF.toInt()
        )
    }

    override fun start() {
        toolbarFeature.start()
    }

    override fun stop() {
        toolbarFeature.stop()
    }
}
