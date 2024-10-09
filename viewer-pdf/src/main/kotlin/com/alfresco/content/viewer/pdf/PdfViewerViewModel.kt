package com.alfresco.content.viewer.pdf

import android.net.Uri
import androidx.webkit.WebViewAssetLoader
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.kotlin.filename
import com.alfresco.kotlin.isLocalPath
import com.alfresco.kotlin.parentFile

data class PdfViewerState(
    val uri: String,
) : MavericksState {
    constructor(args: ChildViewerArgs) : this(args.uri)
}

class PdfViewerViewModel(state: PdfViewerState) : MavericksViewModel<PdfViewerState>(state) {
    fun assetDomain(state: PdfViewerState): String =
        if (state.uri.isLocalPath()) {
            WebViewAssetLoader.DEFAULT_DOMAIN
        } else {
            Uri.parse(state.uri).authority ?: ""
        }

    /**
     * returns the base url.
     */
    fun baseUrl(state: PdfViewerState) =
        if (state.uri.isLocalPath()) {
            "https://${WebViewAssetLoader.DEFAULT_DOMAIN}"
        } else {
            val docUri = Uri.parse(state.uri)
            "${docUri.scheme}://${docUri.authority}"
        }

    /**
     * returns the asset url
     */
    fun assetUrl(state: PdfViewerState) =
        if (state.uri.isLocalPath()) {
            val filename = state.uri.filename()
            "https://${WebViewAssetLoader.DEFAULT_DOMAIN}/$RESERVED_FILES_PATH/$filename"
        } else {
            state.uri
        }

    /**
     * returns the viewer url
     */
    fun viewerUrl(state: PdfViewerState) =
        if (state.uri.contains(
                "#/preview",
            )
        ) {
            state.uri.replace("/aca", "").plus("?mobileapps=true")
        } else {
            "${baseUrl(state)}/$RESERVED_ASSETS_PATH/viewer.html"
        }

    /**
     * it returns true if uri is from local directories otherwise false
     */
    fun localDir(state: PdfViewerState) =
        if (state.uri.isLocalPath()) {
            requireNotNull(state.uri.parentFile())
        } else {
            null
        }

    companion object {
        const val RESERVED_ASSETS_PATH = "--assets--"
        const val RESERVED_FILES_PATH = "--files--"
    }
}
