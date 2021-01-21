package com.alfresco.content.viewer

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxState
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.RenditionRepository
import java.io.File
import java.lang.UnsupportedOperationException
import kotlinx.coroutines.launch

enum class ViewerType {
    Pdf,
    Image,
    Text,
    Media
}

data class ViewerState(
    val id: String,
    val entry: Entry? = null,
    val ready: Boolean = false,
    val viewerType: ViewerType? = null,
    val viewerUri: String? = null
) : MvRxState {
    constructor(args: ViewerArgs) : this(args.id)
}

class ViewerViewModel(
    state: ViewerState
) : MvRxViewModel<ViewerState>(state) {

    private val offlineRepository = OfflineRepository()
    private val browseRepository = BrowseRepository()

    init {
        viewModelScope.launch {
            try {
                val entry = fetchEntry(state.id)
                setState { copy(entry = entry) }

                val type = supportedViewerType(entry)
                if (type != null) {
                    setState { copy(
                        ready = true,
                        viewerType = type,
                        viewerUri = getContentUri(entry)
                    ) }
                } else {
                    if (entry.offlineStatus == OfflineStatus.Synced) {
                        val renditionUri = offlineRenditionUri(entry)
                        val type = if (renditionUri.contains("pdf")) ViewerType.Pdf else ViewerType.Image
                        setState { copy(
                            ready = true,
                            viewerType = type,
                            viewerUri = renditionUri
                        ) }
                    } else {
                        val renditionUri = RenditionRepository().fetchRenditionUri(state.id)
                        setState { copy(
                            ready = true,
                            viewerType = renditionViewerType(renditionUri),
                            viewerUri = renditionUri
                        ) }
                    }
                }
            } catch (ex: Exception) {
                setState { copy(ready = true) }
            }
        }
    }

    private suspend fun fetchEntry(id: String): Entry {
        val offline = offlineRepository.entry(id)
        return if (
            offline != null &&
            offline.offlineStatus == OfflineStatus.Synced
        ) {
            offline
        } else {
            browseRepository.fetchEntry(id)
        }
    }

    private fun supportedViewerType(entry: Entry): ViewerType? =
        when {
            entry.mimeType == "application/pdf" ->
                ViewerType.Pdf
            imageFormats.contains(entry.mimeType) ->
                ViewerType.Image
            entry.mimeType?.startsWith("text/") == true ->
                ViewerType.Text
            entry.mimeType?.startsWith("audio/") == true ||
                entry.mimeType?.startsWith("video/") == true ->
                ViewerType.Media
            else ->
                null
        }

    private fun getContentUri(entry: Entry) =
        if (entry.offlineStatus == OfflineStatus.Synced) {
            offlineRepository.contentUri(entry)
        } else {
            browseRepository.contentUri(entry)
        }

    private fun renditionViewerType(uri: String?) =
        if (uri != null) {
            if (Uri.parse(uri).pathSegments.contains("pdf")) {
                ViewerType.Pdf
            } else {
                ViewerType.Image
            }
        } else null

    private fun offlineRenditionUri(entry: Entry): String {
        val dir = offlineRepository.contentDir(entry)

        val pdfPath = "${dir.path}/.preview_pdf"
        if (File(pdfPath).exists()) {
            return "file://$pdfPath"
        }

        val imgPath = "${dir.path}/.preview_img"
        if (File(imgPath).exists()) {
            return "file://$imgPath"
        }

        throw UnsupportedOperationException()
    }

    companion object {
        private val imageFormats = setOf("image/bmp", "image/jpeg", "image/png", "image/gif", "image/webp", "image/gif", "image/svg+xml")
    }
}
