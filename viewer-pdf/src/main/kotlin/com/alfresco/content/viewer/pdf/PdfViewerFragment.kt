package com.alfresco.content.viewer.pdf

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.viewer.common.ChildViewerFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class PdfViewerFragment : ChildViewerFragment() {

    private val viewModel: PdfViewerViewModel by fragmentViewModel()
    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.viewer_pdf, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webview)
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        val jsBridge = withState(viewModel) {
            NativeBridge(EglExt.maxTextureSize, it.uri) { reason ->
                activity?.runOnUiThread {
                    showPasswordPrompt(reason)
                }
            }
        }

        webView.settings.apply {
            allowContentAccess = false
            allowFileAccess = false
            allowContentAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            cacheMode = WebSettings.LOAD_NO_CACHE
            javaScriptEnabled = true
            @Suppress("DEPRECATION")
            saveFormData = false
        }

        val assetLoader = makeAssetLoader()

        webView.addJavascriptInterface(jsBridge, "bridge")

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // Open URIs in external browser
                startActivity(Intent(Intent.ACTION_VIEW, request?.url))
                return true
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

        // Loading state is handled by pdf viewer
        loadingListener.get()?.onContentLoaded()
    }

    private fun makeAssetLoader() = withState(viewModel) {
        WebViewAssetLoader.Builder()
            .setDomain(Uri.parse(it.uri).authority ?: "")
            .setHttpAllowed(true)
            .addPathHandler(
                "/$RESERVED_ASSETS_PATH/",
                WebViewAssetLoader.AssetsPathHandler(requireContext())
            )
            .build()
    }

    class NativeBridge(
        @get:JavascriptInterface val maxTextureSize: Int,
        @get:JavascriptInterface val assetUrl: String,
        val onPasswordPrompt: (Int) -> Unit
    ) {
        @JavascriptInterface
        fun showPasswordPrompt(reason: Int) {
            onPasswordPrompt(reason)
        }
    }

    override fun invalidate() {
        loadContent()
    }

    /**
     * Loads the current content unless it's already loaded
     */
    private fun loadContent() =
        withState(viewModel) {
            val docUri = Uri.parse(it.uri)
            val baseUrl = "${docUri.scheme}://${docUri.authority}"
            val targetUrl = "$baseUrl/$RESERVED_ASSETS_PATH/viewer.html"

            if (webView.url != targetUrl) {
                webView.loadUrl(targetUrl)
            }
        }

    /**
     * Displays the password prompt, with [reason] equals 1 if it's the first time.
     */
    private fun showPasswordPrompt(reason: Int) {
        val context = context ?: return

        val view = layoutInflater.inflate(R.layout.view_alert_password, null)
        val input = view.findViewById<TextInputEditText>(R.id.password_input)

        val title = if (reason == 1) getString(R.string.password_prompt_title) else getString(R.string.password_prompt_fail_title)
        val message = if (reason == 1) getString(R.string.password_prompt_message) else getString(R.string.password_prompt_fail_message)
        val alert = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(view)
            .setMessage(message)
            .setPositiveButton(getString(R.string.password_prompt_positive)) { _, _ ->
                webView.evaluateJavascript(
                    "PDFViewerApplication.onPassword(\"${input.text}\")",
                    null
                )
            }
            .setNegativeButton(getString(R.string.password_prompt_negative)) { dialog, _ ->
                dialog.cancel()
            }
            .setOnCancelListener { _ ->
                requireActivity().onBackPressed()
            }.create()

        alert.show()
    }

    private companion object {
        const val RESERVED_ASSETS_PATH = "--assets--"
    }
}
