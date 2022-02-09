package com.alfresco.content.shareextension

import com.airbnb.mvrx.MavericksState
import com.alfresco.content.data.Entry

data class ExtensionViewState(
    val entries: List<Entry> = emptyList()
) : MavericksState
