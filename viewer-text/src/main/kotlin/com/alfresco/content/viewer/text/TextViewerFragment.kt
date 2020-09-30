package com.alfresco.content.viewer.text

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.viewer.common.ChildViewerFragment

/**
 * Viewer for displaying plain text backed by a [WebView]
 * Fragment is retained across configuration changes, and webView is reattached to maintain state.
 */
class TextViewerFragment : ChildViewerFragment(R.layout.viewer_text) {

    private val viewModel: TextViewerViewModel by fragmentViewModel()
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = createWebView(requireContext())
        retainInstance = true
    }

    private fun createWebView(context: Context): WebView {
        return WebView(context).apply {
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.savePassword = false
            settings.saveFormData = false
            settings.blockNetworkLoads = true
            settings.allowFileAccess = true
            settings.javaScriptEnabled = false
            settings.defaultTextEncodingName = "utf-8"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState) as ViewGroup

        view.addView(webView, 0)
        webView.visibility = View.GONE

        // On configuration change content may be loaded
        withState(viewModel) { state ->
            if (isContentLoaded(state)) {
                webView.visibility = View.VISIBLE
                loadingListener.get()?.onContentLoaded()
            }
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()

        if (retainInstance && webView.parent is ViewGroup) {
            (webView.parent as ViewGroup).removeView(webView)
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.path is Success && webView.url != state.path()) {
            webView.visibility = View.VISIBLE
            webView.loadUrl(state.path())
            loadingListener.get()?.onContentLoaded()
        }
    }

    private fun isContentLoaded(state: TextViewerState): Boolean {
        return state.path is Success && webView.url == state.path()
    }
}
