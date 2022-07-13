package com.alfresco.content.listview.tasks

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.listview.R
import com.alfresco.content.listview.databinding.ViewListTaskRowBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewTaskRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListTaskRowBinding.inflate(LayoutInflater.from(context), this, true)
    private var isCompact: Boolean = false

    @ModelProp
    fun setData(entry: TaskEntry) {
        binding.title.text = entry.name
        binding.subtitle.text = entry.assignee.name

        updatePriority(entry.priority)
    }

    @ModelProp
    fun setCompact(compact: Boolean) {
        this.isCompact = compact

        val heightResId = if (compact) R.dimen.list_row_compact_height else R.dimen.list_row_height
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimension(heightResId).toInt()
        )

    }

    private fun updatePriority(priority: String) {

        when (priority) {
            "0", "1", "2", "3" -> binding.tvPriority.apply {
                text = context.getString(R.string.priority_low)
                setTextColor(ContextCompat.getColor(context, R.color.colorPriorityLow))
                background = ContextCompat.getDrawable(context, R.drawable.bg_priority_low)
            }
            "4", "5", "6", "7" -> binding.tvPriority.apply {
                text = context.getString(R.string.priority_medium)
                setTextColor(ContextCompat.getColor(context, R.color.colorPriorityMedium))
                background = ContextCompat.getDrawable(context, R.drawable.bg_priority_medium)
            }
            else -> binding.tvPriority.apply {
                text = context.getString(R.string.priority_high)
                setTextColor(ContextCompat.getColor(context, R.color.colorPriorityHigh))
                background = ContextCompat.getDrawable(context, R.drawable.bg_priority_high)
            }
        }

    }


    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }

    @CallbackProp
    fun setMoreClickListener(listener: OnClickListener?) {
    }
}
