package com.alfresco.content.browse.menu

import com.airbnb.mvrx.MvRxState

data class MenuEntry(
    val path: String,
    val title: String,
    val icon: Int
)

data class BrowseMenuViewState(
    val entries: List<MenuEntry> = emptyList()
) : MvRxState
