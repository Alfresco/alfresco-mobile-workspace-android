package com.alfresco.content.browse.menu

import com.airbnb.mvrx.MavericksState
import com.alfresco.content.data.PageView

data class MenuEntry(
    val path: String,
    val title: String,
    val icon: Int,
    var pageView: PageView
)

data class BrowseMenuViewState(
    val entries: List<MenuEntry> = emptyList()
) : MavericksState
