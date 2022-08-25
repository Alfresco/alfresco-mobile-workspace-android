package com.alfresco.content.listview

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.alfresco.content.listview.tasks.TaskPriority
import com.alfresco.content.listview.tasks.getTaskPriority

/**
 * update the priority view as per high,medium and low
 */
fun TextView.updatePriorityView(priority: Int) {
    when (getTaskPriority(priority)) {
        TaskPriority.LOW -> {
            text = context.getString(R.string.priority_low)
            setTextColor(ContextCompat.getColor(context, R.color.colorPriorityLow))
            background = ContextCompat.getDrawable(context, R.drawable.bg_priority_low)
        }
        TaskPriority.MEDIUM -> {
            text = context.getString(R.string.priority_medium)
            setTextColor(ContextCompat.getColor(context, R.color.colorPriorityMedium))
            background = ContextCompat.getDrawable(context, R.drawable.bg_priority_medium)
        }
        TaskPriority.HIGH -> {
            text = context.getString(R.string.priority_high)
            setTextColor(ContextCompat.getColor(context, R.color.colorPriorityHigh))
            background = ContextCompat.getDrawable(context, R.drawable.bg_priority_high)
        }
        else -> text = context.getString(R.string.priority_none)
    }
}
