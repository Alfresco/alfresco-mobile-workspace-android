package com.alfresco.content.browse.menu

import com.airbnb.mvrx.MavericksState

data class MenuEntry(
    val path: String,
    val title: String,
    val icon: Int
)

data class BrowseMenuViewState(
    val entries: List<MenuEntry> = emptyList()
) : MavericksState
