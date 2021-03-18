package com.alfresco.content.browse.menu

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import androidx.annotation.ArrayRes
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.browse.R
import com.alfresco.content.data.BrowseRepository

class BrowseMenuViewModel(
    viewState: BrowseMenuViewState,
    context: Context
) : MavericksViewModel<BrowseMenuViewState>(viewState) {

    init {
        val tiles = context.resources.getStringArray(R.array.browse_menu_titles)
        val icons = context.resources.getResourceList(R.array.browse_menu_icons)
        val paths = context.resources.getStringArray(R.array.browse_menu_paths)
        val entries = tiles.zip(icons).zip(paths) { (t, i), p -> MenuEntry(p, t, i) }

        setState { copy(entries = entries) }
    }

    fun getMyFilesNodeId() = BrowseRepository().myFilesNodeId

    companion object : MavericksViewModelFactory<BrowseMenuViewModel, BrowseMenuViewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: BrowseMenuViewState
        ) = BrowseMenuViewModel(state, viewModelContext.app())
    }
}

private fun Resources.getResourceList(@ArrayRes id: Int): MutableList<Int> {
    val typedArray = this.obtainTypedArray(id)
    val list = typedArray.toResourceList()
    typedArray.recycle()
    return list
}

private fun TypedArray.toResourceList(): MutableList<Int> {
    val list = mutableListOf<Int>()
    for (i in 0 until this.length()) {
        list.add(this.getResourceId(i, 0))
    }
    return list
}
