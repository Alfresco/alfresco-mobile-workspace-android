package com.alfresco.content.listview

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.alfresco.content.listview.tasks.TaskPriority
import com.alfresco.content.listview.tasks.getTaskPriority
import com.google.android.material.color.MaterialColors

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

/**
 * It adds the read more text if line exceeds more than 4 lines
 */
fun TextView.addReadMore() {
    val readMoreText = "...Read more"
    val maxLineCount = 4
    if (layout.lineCount >= maxLineCount) {
        val lineEndIndex = layout.getLineEnd(maxLineCount - 1)

        val truncatedText = "${text.subSequence(0, lineEndIndex - readMoreText.length + 1)}$readMoreText"

        val spannable: Spannable = SpannableString(truncatedText)

        spannable.setSpan(
            ForegroundColorSpan(MaterialColors.getColor(this.context, R.attr.colorPrimary, Color.BLUE)),
            truncatedText.length - readMoreText.length,
            truncatedText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        setText(spannable, TextView.BufferType.SPANNABLE)

        maxLines = maxLineCount
    }
}
