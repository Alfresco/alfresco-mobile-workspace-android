package com.alfresco.content.viewer.text

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.viewer.common.ChildViewerFragment
import com.alfresco.kotlin.filename

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
    ): View {
        webView = createWebView(requireContext(), makeAssetLoader())
        webView.visibility = View.GONE
        return webView
    }

    @Suppress("DEPRECATION")
    private fun createWebView(context: Context, assetLoader: WebViewAssetLoader) =
        WebView(context).apply {
            with(settings) {
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                savePassword = false
                saveFormData = false
                blockNetworkLoads = true
                allowContentAccess = false
                allowFileAccess = false
                @Suppress("DEPRECATION")
                allowFileAccessFromFileURLs = false
                @Suppress("DEPRECATION")
                allowUniversalAccessFromFileURLs = false
                javaScriptEnabled = false
                defaultTextEncodingName = "utf-8"
            }

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    if (request?.method != "GET") {
                        return null
                    }

                    return assetLoader.shouldInterceptRequest(request.url)
                }
            }
        }

    private fun makeAssetLoader() =
        WebViewAssetLoader.Builder()
            .addPathHandler(
                "/",
                WebViewAssetLoader.InternalStoragePathHandler(
                    requireContext(),
                    viewModel.docPath
                )
            )
            .build()

    override fun invalidate() = withState(viewModel) { state ->
        if (state.path is Success && webView.url != state.path()) {
            webView.visibility = View.VISIBLE
            val filename = state.path()?.filename()
            webView.loadUrl("https://${WebViewAssetLoader.DEFAULT_DOMAIN}/$filename")
            loadingListener.get()?.onContentLoaded()
        }
    }
}
