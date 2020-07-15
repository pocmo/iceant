package de.pocmo.iceant

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import de.pocmo.iceant.browser.BrowserFragment
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineView
import javax.inject.Inject

@AndroidEntryPoint
class BrowserActivity : AppCompatActivity() {
    @Inject lateinit var engine: Engine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, BrowserFragment())
                .commit()
        }
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
