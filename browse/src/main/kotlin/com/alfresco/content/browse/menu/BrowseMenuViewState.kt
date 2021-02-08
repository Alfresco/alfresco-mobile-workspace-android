package com.alfresco.content.browse.menu

import com.airbnb.mvrx.MvRxState

data class FileEntry(
    val path: String,
    val title: String,
    val icon: Int
)

data class BrowseMenuViewState(
    val entries: List<FileEntry> = emptyList()
) : MvRxState
