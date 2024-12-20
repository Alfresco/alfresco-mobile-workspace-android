package com.alfresco.content.viewer

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.Rendition
import com.alfresco.content.data.RenditionRepository
import kotlinx.coroutines.launch

data class ViewerState(
    val id: String,
    val mode: String,
    val entry: Entry? = null,
    val ready: Boolean = false,
    val viewerUri: String? = null,
    val viewerMimeType: String? = null,
) : MavericksState {
    constructor(args: ViewerArgs) : this(args.id, args.mode)
}

class ViewerViewModel(
    state: ViewerState,
) : MavericksViewModel<ViewerState>(state) {
    private lateinit var offlineRepository: OfflineRepository
    private lateinit var browseRepository: BrowseRepository
    private lateinit var renditionRepository: RenditionRepository
    private lateinit var analyticsManager: AnalyticsManager

    init {
        if (state.mode != "share") {
            offlineRepository = OfflineRepository()
            browseRepository = BrowseRepository()
            renditionRepository = RenditionRepository()
            analyticsManager = AnalyticsManager()
        }
        viewModelScope.launch {
            try {
                if (state.mode == "local") {
                    loadContent(state.id, OfflineContentLoader(offlineRepository))
                } else if (state.mode == "share") {
                    setState {
                        copy(
                            ready = true,
                            viewerUri = state.id,
                            viewerMimeType = "application/pdf",
                        )
                    }
                } else {
                    loadContent(state.id, RemoteContentLoader(browseRepository, renditionRepository))
                }
            } catch (ex: Exception) {
                setState { copy(ready = true) }
            }
        }
    }

    private suspend fun loadContent(
        id: String,
        loader: ContentLoader,
    ) {
        val entry = loader.fetchEntry(id)
        requireNotNull(entry)
        val mimeType = entry.mimeType
        setState { copy(entry = entry) }

        if (ViewerRegistry.isPreviewSupported(mimeType)) {
            setState {
                copy(
                    ready = true,
                    viewerUri = loader.contentUri(entry),
                    viewerMimeType = mimeType,
                )
            }
            analyticsManager.previewFile(
                mimeType ?: "",
                entry.name.substringAfterLast(".", ""),
                true,
            )
        } else {
            val rendition = loader.rendition(entry)
            analyticsManager.previewFile(
                mimeType ?: "",
                entry.name.substringAfterLast(".", ""),
                false,
            )
            requireNotNull(rendition)
            // TODO: isRendition supported
            setState {
                copy(
                    ready = true,
                    viewerUri = rendition.uri,
                    viewerMimeType = rendition.mimeType,
                )
            }
        }
    }

    private interface ContentLoader {
        suspend fun fetchEntry(id: String): Entry?

        fun contentUri(entry: Entry): String

        suspend fun rendition(entry: Entry): Rendition?
    }

    private class RemoteContentLoader(
        val browseRepository: BrowseRepository,
        val renditionRepository: RenditionRepository,
    ) : ContentLoader {
        override suspend fun fetchEntry(id: String) = browseRepository.fetchEntry(id)

        override fun contentUri(entry: Entry) = browseRepository.contentUri(entry)

        override suspend fun rendition(entry: Entry) = renditionRepository.fetchRendition(entry.id)
    }

    private class OfflineContentLoader(
        val offlineRepository: OfflineRepository,
    ) : ContentLoader {
        override suspend fun fetchEntry(id: String) = offlineRepository.entry(id)

        override fun contentUri(entry: Entry) = offlineRepository.contentUri(entry)

        override suspend fun rendition(entry: Entry): Rendition {
            val dir = offlineRepository.contentDir(entry)
            return Rendition.fetchRenditionInPath(dir.path)
        }
    }
}
