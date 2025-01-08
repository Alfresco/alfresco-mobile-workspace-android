package com.alfresco.content.shareextension

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import androidx.annotation.ArrayRes
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.browse.menu.MenuEntry
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.PageView

/**
 * Mark as ExtensionViewState
 */
data class ExtensionViewState(
    val path: String,
    val entries: List<MenuEntry> = emptyList(),

    ) : MavericksState {
    constructor(args: ExtensionArgs) : this(args.path)
}

/**
 * Mark as ExtensionViewModel
 */
class ExtensionViewModel(
    state: ExtensionViewState,
    val context: Context,
) : MavericksViewModel<ExtensionViewState>(state) {

    init {
        val tiles = context.resources.getStringArray(R.array.share_menu_titles)
        val icons = context.resources.getResourceList(R.array.share_menu_icons)
        val paths = context.resources.getStringArray(R.array.share_menu_paths)
        val entries = tiles.zip(icons).zip(paths) { (t, i), p -> MenuEntry(p, t, i, getPageView(t)) }

        setState { copy(entries = entries) }
    }

    private fun getPageView(path: String): PageView =
        when (path) {
            context.getString(R.string.browse_menu_personal) -> PageView.PersonalFiles
            context.getString(R.string.browse_menu_my_libraries) -> PageView.MyLibraries
            else -> PageView.None
        }

    /**
     * returns the nodeID for my files
     */
    fun getMyFilesNodeId() = BrowseRepository().myFilesNodeId

    companion object : MavericksViewModelFactory<ExtensionViewModel, ExtensionViewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ExtensionViewState,
        ) = ExtensionViewModel(state, viewModelContext.app())
    }
}

private fun Resources.getResourceList(
    @ArrayRes id: Int,
): MutableList<Int> {
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