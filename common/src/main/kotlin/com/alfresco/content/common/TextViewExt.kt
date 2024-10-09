package com.alfresco.content.common

import android.content.Context
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
import com.alfresco.content.data.TaskPriority
import com.alfresco.content.data.getTaskPriority

typealias TextViewCallback = ((isClicked: Boolean) -> Unit)?

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

        TaskPriority.RESET -> {
            text = context.getString(R.string.none)
            setPadding(0, 0, 0, 0)
            setTextColor(ContextCompat.getColor(context, R.color.colorGray1))
            background = null
        }

        else -> text = context.getString(R.string.priority_none)
    }
}

/**
 * It will return true if textview has ellipsized at end otherwise false
 */
fun TextView.isEllipsized() = layout.text.toString() != text.toString()

/**
 * It adds the read more text if line exceeds more than 4 lines
 */
fun TextView.addTextViewPrefix(
    prefix: String,
    callback: TextViewCallback,
) {
    if (layout == null) {
        return
    }
    if (layout.lineCount < 4) {
        return
    }
    val hasSpaces = text.contains(" ")
    val maxLineCount = if (hasSpaces) 3 else 2
    if (layout.lineCount >= maxLineCount) {
        val startCount = 0
        val truncatedText = getTruncatedText(this, maxLineCount, startCount, prefix)
        val spannable: Spannable = SpannableString(truncatedText)
        spannable.setSpan(
            addClickableSpan(context, callback),
            0,
            truncatedText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorBlue)),
            truncatedText.length - prefix.length.minus(1),
            truncatedText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        setText(spannable, TextView.BufferType.SPANNABLE)
        maxLines = 4
        movementMethod = LinkMovementMethod.getInstance()
    }
}

private fun getTruncatedText(
    textView: TextView,
    maxLineCount: Int,
    count: Int,
    prefix: String,
): String {
    var startCount = count
    val newString: StringBuilder = StringBuilder("")
    for (i in 0 until maxLineCount) {
        val lineEnd = textView.layout.getLineEnd(i)
        if (i != maxLineCount.minus(1)) {
            newString.append(textView.text.subSequence(startCount, lineEnd))
        } else {
            if (lineEnd.minus(startCount) > prefix.length + 1) {
                newString.append(textView.text.subSequence(startCount, lineEnd - (prefix.length + 1)).toString().replace("\n", ""))
                newString.append(" $prefix")
            } else {
                newString.append("${textView.text.subSequence(startCount, lineEnd).toString().replace("\n", "")} $prefix")
            }
        }
        startCount = lineEnd
    }
    return newString.toString()
}

private fun addClickableSpan(
    context: Context,
    callback: TextViewCallback,
): ClickableSpan {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(R.attr.colorControlNormal, typedValue, true)
    // Clickable Span will help us to make clickable a text
    val clickableSpan =
        object : ClickableSpan() {
            override fun onClick(textView: View) {
                callback?.invoke(true)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = typedValue.data
            }
        }
    return clickableSpan
}
