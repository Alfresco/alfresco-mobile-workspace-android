package com.alfresco.content.data

import android.content.Context
import java.math.RoundingMode
import java.text.DecimalFormat

const val chipTextDisplayLimit = 30

/**
 * returns the formatted text string as per chip display conditions
 */
fun String.wrapWithLimit(context: Context, limit: Int, delimiter: String? = null, multipleValue: Boolean = false): String {
    if (this.length <= limit && delimiter == null) {
        return this
    }

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

    return if (multipleValue) {
        context.getString(R.string.name_truncate_in, this.take(5), this.takeLast(5))
    } else
        context.getString(R.string.name_truncate_end, this.take(chipTextDisplayLimit))
}

/**
 * converting Byte to KB up tp 2 decimal point.
 */
fun String.byteToKB(): String {
    val df = DecimalFormat("#.00")
    df.roundingMode = RoundingMode.FLOOR
    return df.format(this.toDouble().div(1000)).toString()
}

/**
 * converting KB to byte up to 0 decimal point.
 */
fun String.kBToByte(): String {
    return "%.0f".format(this.toDouble().times(1000)).toString()
}
