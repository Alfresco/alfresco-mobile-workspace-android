package com.alfresco.content.viewer

import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxState
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.RenditionRepository
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
                val entry = with(offlineRepository.entry(state.id)) {
                    if (this == null || this.offlineStatus != OfflineStatus.Synced) {
                        browseRepository.fetchEntry(state.id)
                    } else {
                        this
                    }
                }

                setState { copy(entry = entry) }

                val result = makeViewerConfig(entry)
                if (result.first != null) {
                    setState {
                        copy(
                            ready = true,
                            viewerType = result.first,
                            viewerUri = result.second
                        )
                    }
                } else {
                    val renditionUri = RenditionRepository().fetchRenditionUri(state.id)
                    val renditionType = if (renditionUri != null) {
                        if (renditionUri.contains("pdf")) ViewerType.Pdf else ViewerType.Image
                    } else null
                    setState {
                        copy(
                            ready = true,
                            viewerType = renditionType,
                            viewerUri = renditionUri
                        )
                    }
                }
            } catch (_: Exception) {
                setState { copy(ready = true) }
            }
        }
    }

    private fun makeViewerConfig(entry: Entry): Pair<ViewerType?, String> =
        Pair(when {
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
        }, getContentUri(entry))

    private fun getContentUri(entry: Entry) =
        if (entry.offlineStatus == OfflineStatus.Synced) {
            offlineRepository.contentUri(entry.id)
        } else {
            browseRepository.contentUri(entry.id)
        }

    companion object {
        private val imageFormats = setOf("image/bmp", "image/jpeg", "image/png", "image/gif", "image/webp", "image/gif", "image/svg+xml")
    }
}
