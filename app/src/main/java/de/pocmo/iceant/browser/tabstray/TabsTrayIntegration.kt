package de.pocmo.iceant.browser.tabstray

import android.graphics.Color
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.pocmo.iceant.R
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.tabstray.DefaultTabViewHolder
import mozilla.components.browser.tabstray.TabsAdapter
import mozilla.components.browser.tabstray.TabsTrayStyling
import mozilla.components.browser.tabstray.ViewHolderProvider
import mozilla.components.browser.thumbnails.loader.ThumbnailLoader
import mozilla.components.browser.thumbnails.storage.ThumbnailStorage
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.feature.tabs.tabstray.TabsFeature
import mozilla.components.support.base.feature.LifecycleAwareFeature

class TabsTrayIntegration(
    store: BrowserStore,
    recyclerView: RecyclerView,
    addTabButton: Button,
    drawer: DrawerLayout,
    tabsUseCases: TabsUseCases,
    private val thumbnailStorage: ThumbnailStorage
) : LifecycleAwareFeature {
    private val tabsFeature = TabsFeature(
        createAndSetupTabsTray(recyclerView),
        store,
        tabsUseCases.selectTab,
        tabsUseCases.removeTab
    ) {
        drawer.close()
    }

    init {
        addTabButton.setOnClickListener {
            tabsUseCases.addTab("about:blank", selectTab = true)
            drawer.close()
        }
    }

    override fun start() {
        tabsFeature.start()
    }

    override fun stop() {
        tabsFeature.stop()
    }

    private fun createAndSetupTabsTray(
        recyclerView: RecyclerView
    ): TabsTray {
        recyclerView.layoutManager = LinearLayoutManager(
            recyclerView.context, LinearLayoutManager.VERTICAL, false
        )

        val loader = ThumbnailLoader(thumbnailStorage)

        val viewHolderProvider: ViewHolderProvider = { viewGroup ->
            val view = LayoutInflater.from(recyclerView.context)
                .inflate(R.layout.item_tab, viewGroup, false)

            DefaultTabViewHolder(view, loader)
        }

        val adapter = TabsAdapter(
            thumbnailLoader = loader,
            viewHolderProvider = viewHolderProvider
        ).apply {
            styling = TabsTrayStyling(
                itemBackgroundColor = Color.TRANSPARENT,
                selectedItemBackgroundColor = ContextCompat.getColor(recyclerView.context, R.color.teal200),
                itemTextColor = Color.WHITE,
                selectedItemTextColor = Color.WHITE,
                itemUrlTextColor = Color.WHITE,
                selectedItemUrlTextColor = Color.WHITE
            )
        }

        recyclerView.adapter = adapter

        return adapter
    }
}
