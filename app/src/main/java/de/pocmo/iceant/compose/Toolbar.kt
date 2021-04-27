package de.pocmo.iceant.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import de.pocmo.iceant.compose.ext.observeAsState
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore

@Composable
fun Toolbar(
    store: BrowserStore,
    onLoadUrl: (String) -> Unit
) {
    val editMode = remember { mutableStateOf(false) }
    val url = store.observeAsState { state ->
        state.selectedTab?.content?.url ?: "<empty>"
    }

    if (editMode.value) {
        EditToolbar(
            url = url.value,
            onCommit = { text ->
                editMode.value = false
                onLoadUrl(text)
            }
        )
    } else {
        DisplayToolbar(
            url = url.value,
            onClick = { editMode.value = true }
        )
    }
}

@Composable
fun DisplayToolbar(
    url: String,
    onClick: () -> Unit
) {
    Text(
        text = url,
        maxLines = 1,
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun EditToolbar(
    url: String,
    onCommit: (String) -> Unit
) {
    val text = remember { mutableStateOf(url) }

    TextField(
        value = text.value,
        singleLine = true,
        onValueChange = { value -> text.value = value },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Go
        ),
        keyboardActions = KeyboardActions(
            onGo = { onCommit(text.value) }
        ),
    )
}
