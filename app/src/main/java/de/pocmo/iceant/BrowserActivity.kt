package de.pocmo.iceant

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.usecases.EngineSessionUseCases
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.toolbar.ToolbarFeature

class BrowserActivity : Activity() {
    private val engine: Engine by lazy { GeckoEngine(this) }
    private val store: BrowserStore by lazy { BrowserStore() }
    private val sessionManager: SessionManager by lazy { SessionManager(engine, store) }

    private lateinit var sessionFeature: SessionFeature
    private lateinit var toolbarFeature: ToolbarFeature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_browser)

        val sessionUseCases = SessionUseCases(sessionManager)
        val engineUseCases = EngineSessionUseCases(sessionManager)

        sessionFeature = SessionFeature(
            store,
            sessionUseCases.goBack,
            engineUseCases,
            findViewById<View>(R.id.engineView) as EngineView
        )

        toolbarFeature = ToolbarFeature(
            findViewById<BrowserToolbar>(R.id.toolbar),
            store,
            sessionUseCases.loadUrl
        )

        sessionManager.add(
            Session("https://www.mozilla.org")
        )
    }

    override fun onStart() {
        super.onStart()

        sessionFeature.start()
        toolbarFeature.start()
    }

    override fun onStop() {
        super.onStop()

        sessionFeature.stop()
        toolbarFeature.stop()
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        if (name == EngineView::class.java.name) {
            return engine.createView(context, attrs).asView()
        }

        return super.onCreateView(name, context, attrs)
    }
}
