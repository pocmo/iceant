package de.pocmo.iceant

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.observe
import javax.inject.Inject

@AndroidEntryPoint
class BrowserComposeActivity : AppCompatActivity() {
    @Inject lateinit var store: BrowserStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Browser(store)
        }
    }
}

@Composable
fun Browser(store: BrowserStore) {
    Column {
        Toolbar(store)
        WebContent()
    }
}

@Composable
fun <R> BrowserStore.observeAsState(map: (BrowserState) -> R): State<R> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { mutableStateOf(map(state)) }

    observe(lifecycleOwner) { browserState ->
        state.value = map(browserState)
    }

    return state
}

@Composable
fun Toolbar(store: BrowserStore) {
    val url = store.observeAsState { state ->
        state.selectedTab?.content?.url ?: "<empty>"
    }

    DisplayToolbar(url.value)
}

@Composable
fun WebContent() {
    Text("Content")
}

@Composable
fun DisplayToolbar(
    url: String
) {
    Text(url)
}
