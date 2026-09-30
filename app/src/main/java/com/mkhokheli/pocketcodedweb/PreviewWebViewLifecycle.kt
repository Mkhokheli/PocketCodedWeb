package com.mkhokheli.pocketcodedweb

import android.webkit.WebView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/** Release the embedded page's CPU while the user previews in another browser. */
internal class PreviewWebViewLifecycle(
    private val view: WebView,
    private val lifecycle: Lifecycle,
) : AutoCloseable {
    private var paused = false
    private val observer = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> pause()
            Lifecycle.Event.ON_START -> resume()
            else -> Unit
        }
    }

    init {
        lifecycle.addObserver(observer)
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) pause()
    }

    private fun pause() {
        if (paused) return
        paused = true
        view.onPause()
        // onPause alone leaves JavaScript running. This affects only WebViews
        // in our app process, not Chrome or the separate Node service.
        view.pauseTimers()
    }

    private fun resume() {
        if (!paused) return
        paused = false
        view.resumeTimers()
        view.onResume()
    }

    override fun close() {
        lifecycle.removeObserver(observer)
        // WebView timers are process-wide; never leave a future preview paused.
        resume()
    }
}
