package com.alfresco.content.viewer.text

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alfresco.content.viewer.common.ContentDownloader
import java.io.File
import kotlinx.coroutines.launch

class TextViewerFragment(
    private val uri: String
) : Fragment(R.layout.viewer_text) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = view.findViewById<WebView>(R.id.webview)
        val progressIndicator = view.findViewById<ProgressBar>(R.id.progress_indicator)

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

        val output = File(requireContext().cacheDir, "content.tmp")

        lifecycleScope.launch {
            ContentDownloader.downloadFileTo(uri, output.path)
            webView.loadUrl("file://${output.path}")
        }
    }
}
