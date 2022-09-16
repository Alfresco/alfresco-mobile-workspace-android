package com.alfresco.content.listview

import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
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

/**
 * It adds the read more text if line exceeds more than 4 lines
 */
fun TextView.addReadMore(prefix: String, callback: ((isClicked: Boolean) -> Unit)?) {
    println("")
    if (layout == null)
        return
    if (layout.lineCount < 4)
        return
    val hasSpaces = text.contains(" ")
    val maxLineCount = if (hasSpaces) 3 else 2
    if (layout.lineCount >= maxLineCount) {
        var startCount = 0
        val newString: StringBuilder = StringBuilder("")
        for (i in 0 until maxLineCount) {
            val lineEnd = layout.getLineEnd(i)
            if (i != maxLineCount.minus(1))
                newString.append(text.subSequence(startCount, lineEnd))
            else {
                if (lineEnd.minus(startCount) > prefix.length + 1) {
                    newString.append(text.subSequence(startCount, lineEnd - (prefix.length + 1)).toString().replace("\n", ""))
                    newString.append(" $prefix")
                } else newString.append("${text.subSequence(startCount, lineEnd).toString().replace("\n", "")} $prefix")
            }
            startCount = lineEnd
        }

        val truncatedText = newString.toString()

        val spannable: Spannable = SpannableString(truncatedText)
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorControlNormal, typedValue, true)

        // Clickable Span will help us to make clickable a text
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                callback?.invoke(true)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = typedValue.data
            }
        }
        spannable.setSpan(
            clickableSpan, 0,
            truncatedText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.alfresco_blue_700)),
            truncatedText.length - prefix.length,
            truncatedText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        setText(spannable, TextView.BufferType.SPANNABLE)
        maxLines = 4
        movementMethod = LinkMovementMethod.getInstance()
    }
}
