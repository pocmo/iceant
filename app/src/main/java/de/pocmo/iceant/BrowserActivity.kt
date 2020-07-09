package de.pocmo.iceant

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineView

class BrowserActivity : Activity() {
    private val engine: Engine by lazy { GeckoEngine(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_browser)
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        if (name == EngineView::class.java.name) {
            val engineView = engine.createView(context, attrs)

            val engineSession = engine.createSession()
            engineSession.loadUrl("https://www.mozilla.org")

            engineView.render(engineSession)

            return engineView.asView()
        }

        return super.onCreateView(name, context, attrs)
    }
}
