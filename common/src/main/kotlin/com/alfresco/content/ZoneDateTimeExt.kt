package com.alfresco.content

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val DATE_FORMAT_1 = "yyyy-MM-dd"
const val DATE_FORMAT_2 = "dd-MMM-yyyy"
const val DATE_FORMAT_2_1 = "dd-MM-yyyy"
const val DATE_FORMAT_3 = "yyyy-MM-dd'T'hh:mm:ss.SSSZ"
const val DATE_FORMAT_3_1 = "yyyy-MM-dd'T'hh:mm:ssZ"
const val DATE_FORMAT_4 = "dd MMM yyyy"
const val DATE_FORMAT_4_1 = "dd MMM yyyy hh:mm a"
const val DATE_FORMAT_5 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
const val DATE_FORMAT_6 = "yyyy-MM-dd'T'HH:mm:ss"
const val DATE_FORMAT_7 = "dd MMM,yyyy hh:mm:ss a"
const val DATE_FORMAT_8 = "dd-MM-yyyy hh:mm:ss a"

/**
 * pare the string date and returns the Date obj
 * @param format
 */
fun String.parseDate(format: String): Date? {
    return SimpleDateFormat(format, Locale.ENGLISH).parse(this)
}

/**
 * format the date and return the String obj
 * @param format
 * @param date
 */
fun Date.formatDate(format: String, date: Date?): String? {
    val formatter = SimpleDateFormat(format, Locale.ENGLISH)
    if (date != null) {
        return formatter.format(date)
    }
    return null
}

/**
 * parse and format the string date as per given format and returns the String obj
 * @param currentFormat
 * @param convertFormat
 */
fun String.getFormattedDate(currentFormat: String, convertFormat: String): String {
    val date = SimpleDateFormat(currentFormat, Locale.ENGLISH).parse(this)
    val formatter = SimpleDateFormat(convertFormat, Locale.getDefault())
    if (date != null) {
        return formatter.format(date)
    }
    return ""
}

/**
 * convert the UTC format date to Local date and time and returns the String obj
 * @param currentFormat
 * @param convertFormat
 */
fun String.getLocalFormattedDate(currentFormat: String, convertFormat: String): String {
    val parserFormat = SimpleDateFormat(currentFormat, Locale.getDefault())
    parserFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = parserFormat.parse(this)
    val formatter = SimpleDateFormat(convertFormat, Locale.getDefault())
    if (date != null) {
        return formatter.format(date)
    }
    return ""
}

/**
 * convert the UTC format date to Local date and time and returns the String obj
 * @param currentFormat
 * @param convertFormat
 */
fun String.getLocalFormattedDate1(currentFormat: String, convertFormat: String): String {
    val parserFormat = SimpleDateFormat(currentFormat, Locale.getDefault())
    parserFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = parserFormat.parse(this)
    if (date != null) {
        val formatter = SimpleDateFormat(convertFormat, Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone(Locale.getDefault().isO3Language)
        val formattedDate = formatter.format(date)
        return formattedDate
    }
    return ""
}

fun updateDateFormat(originalDateString: String?): String? {
    originalDateString ?: return null

    val (datePart, timePart) = originalDateString.split(" ", limit = 2)
    val updatedDatePart = datePart.split("-").joinToString("-") { part ->
        if (part.equals("MM", ignoreCase = true)) {
            part // Keep "MM" as is
        } else part.lowercase() // Convert "DD" and "YYYY" to lowercase
    }

    return if (timePart.isNotEmpty()) "$updatedDatePart $timePart" else updatedDatePart
}
