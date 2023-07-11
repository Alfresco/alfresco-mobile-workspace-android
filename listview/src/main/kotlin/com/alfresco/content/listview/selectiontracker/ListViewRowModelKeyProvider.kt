package com.alfresco.content.listview.selectiontracker

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyViewHolder
import com.alfresco.content.data.Entry
import com.alfresco.content.listview.ListViewRowModel_

class ListViewRowModelKeyProvider(private val recyclerView: RecyclerView) : ItemKeyProvider<Entry>(SCOPE_MAPPED) {

    override fun getKey(position: Int): Entry? {
        val epoxyHolder = recyclerView.findViewHolderForAdapterPosition(position) as? EpoxyViewHolder
        val model = (epoxyHolder?.model as? ListViewRowModel_)
        return model?.data()
    }

    override fun getPosition(key: Entry): Int {
        val adapter = recyclerView.adapter
        for (i in 0 until adapter?.itemCount.orZero()) {
            val epoxyHolder = recyclerView.findViewHolderForAdapterPosition(i) as? EpoxyViewHolder
            val model = (epoxyHolder?.model as? ListViewRowModel_)
            if (model?.data() == key) {
                return i
            }
        }
        return RecyclerView.NO_POSITION
    }

    fun findKey(position: Int): Entry? {
        return getKey(position)
    }
}

private fun <T> T?.orZero(): T = (this ?: 0) as T
