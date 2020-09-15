package com.alfresco.content.viewer

import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxState
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.RenditionRepository
import kotlinx.coroutines.launch

enum class ViewerType {
    Pdf,
    Image,
    Text
}

data class ViewerState(
    val id: String,
    val mimeType: String?,
    val ready: Boolean = false,
    val viewerType: ViewerType? = null,
    val viewerUri: String? = null
) : MvRxState {
    constructor(args: ViewerArgs) : this(args.id, args.type)
}

class ViewerViewModel(
    state: ViewerState
) : MvRxViewModel<ViewerState>(state) {

    init {
        viewModelScope.launch {
            try {
                val result = getContentUri(state.id, state.mimeType)
                if (result != null) {
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
            } catch (_: Exception) { }
        }
    }

    private fun getContentUri(id: String, mimeType: String?): Pair<ViewerType, String>? {
        when {
            mimeType == "application/pdf" ->
                return Pair(ViewerType.Pdf, BrowseRepository().contentUri(id))
            imageFormats.contains(mimeType) ->
                return Pair(ViewerType.Image, BrowseRepository().contentUri(id))
            mimeType?.startsWith("text/") == true ->
                return Pair(ViewerType.Text, BrowseRepository().contentUri(id))
        }

        return null
    }

    companion object {
        val imageFormats = setOf("image/bmp", "image/jpeg", "image/gif", "image/webp", "image/gif", "image/svg xml")
    }
}
