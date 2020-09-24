package com.alfresco.content.viewer.media

import com.airbnb.mvrx.MvRxState
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.viewer.common.ViewerTypeArgs

data class MediaViewerState(
    val uri: String
) : MvRxState {
    constructor(args: ViewerTypeArgs) : this(args.uri)
}

class MediaViewerViewModel(state: MediaViewerState) :
    MvRxViewModel<MediaViewerState>(state)
