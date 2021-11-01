package com.alfresco.content.search.components

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.ArrayList

internal typealias DatePickerOnSuccess = (String) -> Unit
internal typealias DatePickerOnFailure = () -> Unit

/**
 * Mark as DatePickerBuilder class
 */
data class DatePickerBuilder(
    val context: Context,
    val fromDate: String = "",
    val toDate: String = "",
    val isFrom: Boolean = false,
    var dateFormat: String = "",
    var onSuccess: DatePickerOnSuccess? = null,
    var onFailure: DatePickerOnFailure? = null
) {

    private val dateFormatddMMMyy = "dd-MMM-yy"
    private val dateFormatddMMyyyy = "dd-MM-yyyy"

    /**
     * success callback
     */
    fun onSuccess(callback: DatePickerOnSuccess?) =
        apply { this.onSuccess = callback }

    /**
     * failure callback
     */
    fun onFailure(callback: DatePickerOnFailure?) =
        apply { this.onFailure = callback }

    /**
     * show material date picker
     */
    fun show() {
        val fragmentManager = when (context) {
            is AppCompatActivity -> context.supportFragmentManager
            is Fragment -> context.childFragmentManager
            else -> throw IllegalArgumentException()
        }

        val constraintsBuilder = CalendarConstraints.Builder()
        var endDate = MaterialDatePicker.todayInUtcMilliseconds()
        val validators: ArrayList<CalendarConstraints.DateValidator> = ArrayList()
        var selectionDate = MaterialDatePicker.todayInUtcMilliseconds()

        if (dateFormat.isEmpty())
            dateFormat = dateFormatddMMMyy

        if (isFrom) {
            if (fromDate.isNotEmpty())
                fromDate.getddMMyyyyStringDate()?.let { stringDate ->
                    selectionDate = getSelectionDate(stringDate)
                }

            if (toDate.isNotEmpty())
                toDate.getDateFromString()?.let { date ->
                    endDate = date.time
                }
        } else {
            if (toDate.isNotEmpty())
                toDate.getddMMyyyyStringDate()?.let { stringDate ->
                    selectionDate = getSelectionDate(stringDate)
                }

            if (fromDate.isNotEmpty())
                fromDate.getDateFromString()?.let { date ->
                    validators.add(DateValidatorPointForward.from(date.time))
                }
        }
        validators.add(DateValidatorPointBackward.before(endDate))
        constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators))

        val datePicker = MaterialDatePicker.Builder.datePicker().apply {
            setSelection(selectionDate)
            setCalendarConstraints(constraintsBuilder.build())
        }.build()

        datePicker.show(fragmentManager, DatePickerBuilder::class.java.simpleName)

        datePicker.addOnPositiveButtonClickListener {
            val date = Date(it)
            val stringDate = getFormatDate(date)
            onSuccess?.invoke(stringDate)
        }
    }

    private fun getFormatDate(currentTime: Date): String {
        return SimpleDateFormat(dateFormat, Locale.getDefault(Locale.Category.DISPLAY)).format(currentTime)
    }

    private fun String.getDateFromString(): Date? {
        return SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(this)
    }

    private fun String.getddMMyyyyStringDate(): String? {

        val formatter = SimpleDateFormat(dateFormatddMMyyyy, Locale.ENGLISH)
        val date = SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(this)
        if (date != null)
            return formatter.format(date)

        return null
    }

    private fun getSelectionDate(dateString: String): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val splitDate = dateString.split("-")
        calendar[Calendar.DAY_OF_MONTH] = splitDate[0].toInt()
        calendar[Calendar.MONTH] = splitDate[1].toInt().minus(1)
        calendar[Calendar.YEAR] = splitDate[2].toInt()
        return calendar.timeInMillis
    }
}
