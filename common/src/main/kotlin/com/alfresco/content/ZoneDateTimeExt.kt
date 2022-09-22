package com.alfresco.content

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val DATE_FORMAT_1 = "yyyy-MM-dd"
const val DATE_FORMAT_2 = "dd-MMM-yyyy"
const val DATE_FORMAT_3 = "dd MMM,yyyy"
const val DATE_FORMAT_4 = "dd MMM yyyy"
const val DATE_FORMAT_5 = "yyyy-MM-dd'T'HH:mm:ss'Z'"
const val DATE_FORMAT_6 = "dd-MMM-yy"

/**
 * returns formatted date string for query zone
 */
fun String.parseDate(format: String): Date? {
    return SimpleDateFormat(format, Locale.ENGLISH).parse(this)
}

fun Date.formatDate(format: String, date: Date?): String? {
    val formatter = SimpleDateFormat(format, Locale.ENGLISH)
    if (date != null)
        return formatter.format(date)
    return null
}

fun String.getFormattedDate(currentFormat: String, convertFormat: String): String {
    val date = SimpleDateFormat(currentFormat, Locale.ENGLISH).parse(this)
    val formatter = SimpleDateFormat(convertFormat, Locale.getDefault())
    if (date != null)
        return formatter.format(date)
    return ""
}
