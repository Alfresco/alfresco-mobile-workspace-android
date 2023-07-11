package com.alfresco.content.listview.selectiontracker

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyViewHolder
import com.alfresco.content.data.Entry
import com.alfresco.content.listview.ListViewRowModel_

class ListViewRowModelSelectionLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Entry>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<Entry>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val viewHolder = recyclerView.getChildViewHolder(view)
            if (viewHolder is EpoxyViewHolder) {
                object : ItemDetails<Entry>() {
                    override fun getPosition(): Int {
                        return viewHolder.adapterPosition
                    }

                    override fun getSelectionKey(): Entry? {
                        return (viewHolder.model as? ListViewRowModel_?)?.data()
                    }
                }
            }
        }
        return null
    }
}
