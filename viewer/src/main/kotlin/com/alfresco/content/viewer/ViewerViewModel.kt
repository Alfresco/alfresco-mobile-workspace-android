package com.alfresco.content.viewer

import com.airbnb.mvrx.MvRxState

data class ViewerState(
    val id: String,
    val mimeType: String?
) : MvRxState {
    constructor(args: ViewerArgs) : this(args.id, args.type)
}

// class ViewerViewModel(
//     state: ViewerState,
// ) : MvRxViewModel<ViewerState>(state) {
//
//     init {
//
//     }
// }
