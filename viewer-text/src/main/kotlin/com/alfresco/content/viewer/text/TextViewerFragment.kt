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
 */
class TextViewerFragment : ChildViewerFragment(R.layout.viewer_text) {

    private val viewModel: TextViewerViewModel by fragmentViewModel()
    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        webView = createWebView(requireContext())
        webView.visibility = View.GONE
        return webView
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

    override fun invalidate() = withState(viewModel) { state ->
        if (state.path is Success && webView.url != state.path()) {
            webView.visibility = View.VISIBLE
            webView.loadUrl(state.path() ?: "")
            loadingListener.get()?.onContentLoaded()
        }
    }
}
