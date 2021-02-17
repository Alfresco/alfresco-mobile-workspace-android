package com.alfresco.content.viewer

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxState
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
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
    val mode: String,
    val entry: Entry? = null,
    val ready: Boolean = false,
    val viewerType: ViewerType? = null,
    val viewerUri: String? = null
) : MvRxState {
    constructor(args: ViewerArgs) : this(args.id, args.mode)
}

class ViewerViewModel(
    state: ViewerState
) : MvRxViewModel<ViewerState>(state) {

    private val offlineRepository = OfflineRepository()
    private val browseRepository = BrowseRepository()
    private val renditionRepository = RenditionRepository()

    init {
        viewModelScope.launch {
            try {
                if (state.mode == "local") {
                    loadContent(state.id, OfflineContentLoader(offlineRepository))
                } else {
                    loadContent(state.id, RemoteContentLoader(browseRepository, renditionRepository))
                }
            } catch (ex: Exception) {
                setState { copy(ready = true) }
            }
        }
    }

    private suspend fun loadContent(id: String, loader: ContentLoader) {
        val entry = loader.fetchEntry(id)
        requireNotNull(entry)

        setState { copy(entry = entry) }

        val type = supportedViewerType(entry)
        if (type != null) {
            setState { copy(
                ready = true,
                viewerType = type,
                viewerUri = loader.contentUri(entry)
            ) }
        } else {
            val rendition = loader.rendition(entry)
            setState { copy(
                ready = true,
                viewerType = rendition.second,
                viewerUri = rendition.first
            ) }
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

    companion object {
        private val imageFormats = setOf("image/bmp", "image/jpeg", "image/png", "image/gif", "image/webp", "image/gif", "image/svg+xml")
    }

    private interface ContentLoader {

        suspend fun fetchEntry(id: String): Entry?

        fun contentUri(entry: Entry): String

        suspend fun rendition(entry: Entry): Pair<String, ViewerType>
    }

    private class RemoteContentLoader(
        val browseRepository: BrowseRepository,
        val renditionRepository: RenditionRepository
    ) : ContentLoader {

        override suspend fun fetchEntry(id: String) =
            browseRepository.fetchEntry(id)

        override fun contentUri(entry: Entry) =
            browseRepository.contentUri(entry)

        override suspend fun rendition(entry: Entry): Pair<String, ViewerType> {
            val uri = renditionRepository.fetchRenditionUri(entry.id)
            requireNotNull(uri)
            return Pair(uri, renditionViewerType(uri))
        }

        private fun renditionViewerType(uri: String) =
            if (Uri.parse(uri).pathSegments.contains("pdf")) {
                ViewerType.Pdf
            } else {
                ViewerType.Image
            }
    }

    private class OfflineContentLoader(
        val offlineRepository: OfflineRepository
    ) : ContentLoader {

        override suspend fun fetchEntry(id: String) =
            offlineRepository.entry(id)

        override fun contentUri(entry: Entry) =
            offlineRepository.contentUri(entry)

        override suspend fun rendition(entry: Entry): Pair<String, ViewerType> {
            val dir = offlineRepository.contentDir(entry)

            val pdfPath = "${dir.path}/.preview_pdf"
            if (File(pdfPath).exists()) {
                return Pair("file://$pdfPath", ViewerType.Pdf)
            }

            val imgPath = "${dir.path}/.preview_img"
            if (File(imgPath).exists()) {
                return Pair("file://$imgPath", ViewerType.Image)
            }

            throw UnsupportedOperationException()
        }
    }
}
