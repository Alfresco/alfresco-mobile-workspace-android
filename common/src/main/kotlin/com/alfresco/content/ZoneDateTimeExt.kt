package com.alfresco.content

import java.text.SimpleDateFormat
import java.util.Locale

const val DATE_FORMAT_1 = "yyyy-MM-dd"
const val DATE_FORMAT_2 = "dd-MMM-yyyy"
const val DATE_FORMAT_3 = "dd MMM,yyyy"
const val DATE_FORMAT_4 = "dd MMM yyyy"

/**
 * returns formatted date string for query zone
 */
fun String.getDateZoneFormat(currentFormat: String, convertFormat: String): String {
    val date = SimpleDateFormat(currentFormat, Locale.ENGLISH).parse(this)
    val formatter = SimpleDateFormat(convertFormat, Locale.ENGLISH)
    if (date != null)
        return formatter.format(date)
    return this
}
