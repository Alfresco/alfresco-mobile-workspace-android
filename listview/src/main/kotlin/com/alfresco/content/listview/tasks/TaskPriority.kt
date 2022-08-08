package com.alfresco.content.listview.tasks

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.alfresco.content.listview.R

/**
 * Marked as TaskPriority enum class
 */
enum class TaskPriority(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high")
}

private fun getTaskPriority(priority: String): TaskPriority {
    return when (priority) {
        "0", "1", "2", "3" -> TaskPriority.LOW
        "4", "5", "6", "7" -> TaskPriority.MEDIUM
        else -> TaskPriority.HIGH
    }
}

/**
 * update the priority view as per high,medium and low
 */
fun TextView.updatePriorityView(priority: String) {
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
        else -> {
            text = context.getString(R.string.priority_high)
            setTextColor(ContextCompat.getColor(context, R.color.colorPriorityHigh))
            background = ContextCompat.getDrawable(context, R.drawable.bg_priority_high)
        }
    }
}
