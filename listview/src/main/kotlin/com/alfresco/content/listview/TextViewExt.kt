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
    if (layout == null)
        return
    val readMoreText = "...Read more"
    val maxLineCount = 4
    if (layout.lineCount >= maxLineCount) {
        var startCount = 0
        val newString: StringBuilder = StringBuilder("")
        for (i in 0 until maxLineCount) {
            val lineEnd = layout.getLineEnd(i)
            if (i != maxLineCount.minus(1))
                newString.append(text.subSequence(startCount, lineEnd))
            else {
                if (lineEnd.minus(startCount) > readMoreText.length + 1)
                    newString.append("${text.subSequence(startCount, lineEnd - readMoreText.length + 1).toString().replace("\n", "")}$readMoreText")
                else newString.append("${text.subSequence(startCount, lineEnd).toString().replace("\n", "")}$readMoreText")
            }
            startCount = lineEnd
        }

        val truncatedText = newString.toString()
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
