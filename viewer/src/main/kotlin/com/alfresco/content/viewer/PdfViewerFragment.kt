package com.alfresco.content.viewer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.hideSoftInput
import com.alfresco.content.session.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.IOException
import java.io.InputStream
import java.util.HashMap

class PdfViewerFragment(
    private val documentId: String,
    private val mimeType: String
) : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.viewer_pdf, container, false)
        setHasOptionsMenu(true)
        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webview)
        WebView.setWebContentsDebuggingEnabled(true)

        val uri: String = when (mimeType) {
            "application/pdf" -> BrowseRepository().contentUri(documentId).toString()
            else -> BrowseRepository().renditionUri(documentId).toString()
        }
        val jsBridge = NativeBridge(EglExt.maxTextureSize, uri) { reason ->
            requireActivity().runOnUiThread {
                showPasswordPrompt(reason)
            }
        }

        val settings = webView.settings
        settings.allowContentAccess = false
        settings.allowFileAccess = false
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.javaScriptEnabled = true
        @Suppress("DEPRECATION")
        settings.saveFormData = false

        webView.addJavascriptInterface(jsBridge, "bridge")

        webView.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if (request?.method != "GET") {
                    return null
                }

                if (request.url.path?.contains("$LOCAL_PATH_PREFIX/") != true) {
                    return null
                }

                val path = request.url.path?.removePrefix("/$LOCAL_PATH_PREFIX/")

                if (path == "viewer.html") {
                    val response = fromAsset("text/html", path)
                    val headers = HashMap<String, String>()
                    response?.responseHeaders = headers
                    return response
                }

                if (path?.endsWith(".css") == true) {
                    return fromAsset("text/css", path)
                }

                if (path?.endsWith(".js") == true) {
                    return fromAsset("application/javascript", path)
                }

                return null
            }

            private fun fromAsset(mime: String, path: String): WebResourceResponse? {
                return try {
                    val inputStream: InputStream = requireContext().assets.open(path)
                    WebResourceResponse(mime, null, inputStream)
                } catch (e: IOException) {
                    null
                }
            }
        }
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

    override fun onResume() {
        super.onResume()

        load()
    }

    private fun load() {
        val sessionUri = Uri.parse(SessionManager.requireSession.baseUrl)
        val baseUrl = "${sessionUri.scheme}://${sessionUri.authority}"
        webView.loadUrl("$baseUrl/$LOCAL_PATH_PREFIX/viewer.html")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_viewer_search, menu)

        val searchItem: MenuItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = resources.getString(R.string.search_hint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                hideSoftInput()
                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                return true
            }
        })
    }

    /**
     * Displays the password prompt, with [reason] equals 1 if it's the first time.
     */
    private fun showPasswordPrompt(reason: Int) {
        val context = requireContext()
        val textInputLayout = TextInputLayout(context)
        val padding = TypedValue()
        context.theme.resolveAttribute(R.attr.dialogPreferredPadding, padding, true)
        textInputLayout.setPadding(
            padding.getDimension(resources.displayMetrics).toInt(),
            0,
            padding.getDimension(resources.displayMetrics).toInt(),
            0
        )
        val input = TextInputEditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        textInputLayout.hint = "Password"
        textInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        textInputLayout.addView(input)

        val title = if (reason == 1) getString(R.string.password_prompt_title) else getString(R.string.password_prompt_fail_title)
        val message = if (reason == 1) getString(R.string.password_prompt_message) else getString(R.string.password_prompt_fail_message)
        val alert = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(textInputLayout)
            .setMessage(message)
            .setPositiveButton(getString(R.string.password_prompt_positive)) { dialog, _ ->
                webView.evaluateJavascript(
                    "PDFViewerApplication.onPassword(\"${input.text}\")",
                    null
                )
            }
            .setNegativeButton(getString(R.string.password_prompt_negative)) { dialog, _ ->
                dialog.cancel()
            }
            .setOnCancelListener { dialog ->
                requireActivity().onBackPressed()
            }.create()

        alert.show()
    }

    companion object {
        const val LOCAL_PATH_PREFIX = "fakepath"
    }
}
