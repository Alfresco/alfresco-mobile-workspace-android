package com.alfresco.content.component

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Locale

const val chipTextDisplayLimit = 30

/**
 * returns the formatted text string as per chip display conditions
 */
fun String.wrapWithLimit(context: Context, limit: Int, delimiter: String? = null, multipleValue: Boolean = false): String {
    if (this.length <= limit && delimiter == null)
        return this

    if (delimiter != null) {
        if (this.contains(delimiter)) {
            val splitStringArray = this.split(delimiter)
            val chip1stString = splitStringArray[0]
            if (chip1stString.length > limit) {
                return context.getString(R.string.name_truncate_in_end, chip1stString.wrapWithLimit(context, chipTextDisplayLimit, multipleValue = true), splitStringArray.size.minus(1))
            }
            return context.getString(R.string.name_truncate_in_end, chip1stString, splitStringArray.size.minus(1))
        } else {
            return this.wrapWithLimit(context, chipTextDisplayLimit)
        }
    }

    return if (multipleValue)
        context.getString(R.string.name_truncate_in, this.take(5), this.takeLast(5))
    else
        context.getString(R.string.name_truncate_end, this.take(chipTextDisplayLimit))
}

/**
 * returns formatted date string for query
 */
fun String.getQueryFormat(): String {

    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val date = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH).parse(this)
    if (date != null)
        return formatter.format(date)

    return this
}

/**
 * returns formatted date string for query
 */
fun String.getDateZoneFormat(): String {
    println("zone date -- $this")
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(this)
    val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
    if (date != null)
        return formatter.format(date)

    return this
}
