package com.alfresco.content.viewer.media

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.alfresco.content.viewer.common.ChildViewerArgs

data class MediaViewerState(
    val uri: String
) : MavericksState {
    constructor(args: ChildViewerArgs) : this(args.uri)
}

class MediaViewerViewModel(state: MediaViewerState) :
    MavericksViewModel<MediaViewerState>(state)
