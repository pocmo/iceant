package de.pocmo.iceant.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import de.pocmo.iceant.compose.ext.observeAsState
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineView

@Composable
fun WebContent(store: BrowserStore, engine: Engine) {
    val selectedTab = store.observeAsState { state ->
        // TODO: Data inside the selected tab will change more often and causing a recompose too often
        state.selectedTab
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context -> engine.createView(context).asView() },
        update = { view ->
            val engineView = view as EngineView

            val tab = selectedTab.value
            if (tab == null) {
                engineView.release()
            } else {
                val session = tab.engineState.engineSession
                if (session == null) {
                    store.dispatch(EngineAction.CreateEngineSessionAction(tab.id))
                } else {
                    engineView.render(session)
                }
            }
        }
    )
}
