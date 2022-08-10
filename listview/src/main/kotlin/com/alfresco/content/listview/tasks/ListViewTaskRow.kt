package com.alfresco.content.listview.tasks

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.listview.R
import com.alfresco.content.listview.databinding.ViewListTaskRowBinding
import com.alfresco.content.listview.updatePriorityView

/**
 * Marked as ListViewTaskRow class
 */
@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewTaskRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListTaskRowBinding.inflate(LayoutInflater.from(context), this, true)
    private var isCompact: Boolean = false

    /**
     * set the data on view
     */
    @ModelProp
    fun setData(entry: TaskEntry) {
        binding.title.text = entry.name
        binding.subtitle.text = entry.assignee?.name

        binding.tvPriority.updatePriorityView(entry.priority)
    }

    /**
     * adjust the height of row if subtitle not available
     */
    @ModelProp
    fun setCompact(compact: Boolean) {
        this.isCompact = compact

        val heightResId = if (compact) R.dimen.list_row_compact_height else R.dimen.list_row_height
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimension(heightResId).toInt()
        )
    }

    /**
     * row click listener
     */
    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }
}
