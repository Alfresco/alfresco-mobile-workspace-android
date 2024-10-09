package com.alfresco.content.listview.tasks

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.DATE_FORMAT_6
import com.alfresco.content.DATE_FORMAT_7
import com.alfresco.content.common.updatePriorityView
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.getTaskPriority
import com.alfresco.content.getLocalFormattedDate
import com.alfresco.content.getLocalizedName
import com.alfresco.content.listview.R
import com.alfresco.content.listview.databinding.ViewListTaskRowBinding

/**
 * Marked as ListViewTaskRow class
 */
@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewTaskRow
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val binding = ViewListTaskRowBinding.inflate(LayoutInflater.from(context), this, true)

        /**
         * set the data on view
         */
        @RequiresApi(Build.VERSION_CODES.O)
        @ModelProp
        fun setData(entry: TaskEntry) {
            binding.title.text = entry.name.ifEmpty { context.getString(R.string.title_no_name) }
            val localizedName = context.getLocalizedName(entry.assignee?.name ?: "")
            binding.subtitle.visibility = if (localizedName.trim().isNotEmpty()) View.VISIBLE else View.GONE
            binding.subtitle.text = localizedName
            binding.timeStamp.text = entry.created?.toLocalDateTime().toString().getLocalFormattedDate(DATE_FORMAT_6, DATE_FORMAT_7)
            val accessibilityText =
                context.getString(
                    R.string.accessibility_text_task_row,
                    entry.name,
                    localizedName,
                    getTaskPriority(entry.priority).value,
                )
            binding.parent.contentDescription = accessibilityText

            binding.tvPriority.updatePriorityView(entry.priority)
        }

        /**
         * row click listener
         */
        @CallbackProp
        fun setClickListener(listener: OnClickListener?) {
            setOnClickListener(listener)
        }
    }
