package com.alfresco.content.component

import android.content.Context

const val chipTextDisplayLimit = 30

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
