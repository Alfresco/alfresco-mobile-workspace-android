package com.alfresco.content.viewer.text

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState

class TextViewerFragment : BaseMvRxFragment(R.layout.viewer_text) {

    private val viewModel: TextViewerViewModel by fragmentViewModel()
    private lateinit var webView: WebView
    private lateinit var progressIndicator: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webview)
        progressIndicator = view.findViewById(R.id.progress_indicator)

        val settings = webView.settings
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.savePassword = false
        settings.saveFormData = false
        settings.blockNetworkLoads = true
        settings.allowFileAccess = true
        settings.javaScriptEnabled = false
        settings.defaultTextEncodingName = "utf-8"

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressIndicator.visibility = View.GONE
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.path != null) {
            webView.loadUrl(state.path)
        }
    }
}
