package com.alfresco.content.browse

import com.airbnb.mvrx.MvRxState

data class FileEntry(
    val title: String,
    val icon: Int
)

data class BrowseMenuViewState(
    val entries: List<FileEntry> = emptyList()
) : MvRxState
