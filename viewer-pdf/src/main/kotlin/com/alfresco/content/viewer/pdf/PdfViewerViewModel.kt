package com.alfresco.content.viewer.pdf

import com.airbnb.mvrx.MvRxState
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.viewer.common.ViewerTypeArgs

data class PdfViewerState(
    val uri: String
) : MvRxState {
    constructor(args: ViewerTypeArgs) : this(args.uri)
}

class PdfViewerViewModel(state: PdfViewerState) :
    MvRxViewModel<PdfViewerState>(state)
