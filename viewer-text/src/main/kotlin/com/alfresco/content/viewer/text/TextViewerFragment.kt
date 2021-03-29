package com.alfresco.content.viewer.text

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.WebViewClickSupportListener
import com.alfresco.content.viewer.common.ChildViewerFragment
import com.alfresco.kotlin.filename

/**
 * Viewer for displaying plain text backed by a [WebView]
 */
class TextViewerFragment : ChildViewerFragment(R.layout.viewer_text), MavericksView {

    private val viewModel: TextViewerViewModel by fragmentViewModel()
    private lateinit var webView: WebView

    private var savedInsets: Insets = Insets.NONE

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        webView = createWebView(requireContext(), makeAssetLoader())
        WebView.setWebContentsDebuggingEnabled(true)
        webView.visibility = View.GONE
        webView.setOnTouchListener(WebViewClickSupportListener)
        webView.setOnClickListener(onClickListener)
        ViewCompat.setOnApplyWindowInsetsListener(webView) { v, insets ->
            savedInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            WindowInsetsCompat.CONSUMED
        }
        return webView
    }

    // @Suppress("DEPRECATION")
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
                javaScriptEnabled = true
                defaultTextEncodingName = "utf-8"
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val density = resources.displayMetrics.density
                    val top = savedInsets.top / density // + 52
                    val bottom = savedInsets.bottom / density // + 52
                    webView.loadUrl("javascript:document.body.style.margin=\"${top}px 8px ${bottom}px 8px\"; void 0")
                }

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
            loadingListener?.onContentLoaded()
        }
    }
}
