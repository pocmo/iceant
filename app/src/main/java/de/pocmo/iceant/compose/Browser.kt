package de.pocmo.iceant.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.session.SessionUseCases

@Composable
fun Browser(store: BrowserStore, engine: Engine, sessionUseCases: SessionUseCases) {
    Column {
        Toolbar(
            store = store,
            onLoadUrl = { url -> sessionUseCases.loadUrl(url) }
        )

        WebContent(store, engine)
    }
}
