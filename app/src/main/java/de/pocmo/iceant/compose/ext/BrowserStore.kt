package de.pocmo.iceant.compose.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.observe

@Composable
fun <R> BrowserStore.observeAsState(map: (BrowserState) -> R): State<R> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { mutableStateOf(map(state)) }

    observe(lifecycleOwner) { browserState ->
        state.value = map(browserState)
    }

    return state
}
