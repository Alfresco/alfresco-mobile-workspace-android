package com.alfresco.content.viewer.media

import com.airbnb.mvrx.MvRxState
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.viewer.common.ChildViewerArgs

data class MediaViewerState(
    val uri: String
) : MvRxState {
    constructor(args: ChildViewerArgs) : this(args.uri)
}

class MediaViewerViewModel(state: MediaViewerState) :
    MvRxViewModel<MediaViewerState>(state)
