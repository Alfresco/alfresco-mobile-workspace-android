package com.alfresco.content.component

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alfresco.content.DATE_FORMAT_2_1
import com.alfresco.content.DATE_FORMAT_6
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    var isFutureDate: Boolean = false,
    var onSuccess: DatePickerOnSuccess? = null,
    var onFailure: DatePickerOnFailure? = null,
    var fieldsData: FieldsData? = null,
) {

    private val dateFormatddMMMyy = "dd-MMM-yy"
    private val dateFormatddMMyyyy = "dd-MM-yyyy"
    private val addOneDay = 1000 * 60 * 60 * 24

    init {
        if (dateFormat.isEmpty()) {
            dateFormat = dateFormatddMMMyy
        }
    }

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

        constraintsBuilder.setValidator(CompositeDateValidator.allOf(getValidators(fieldsData)))

        val datePicker = MaterialDatePicker.Builder.datePicker().apply {
            if (fieldsData != null) {
                setTitleText(fieldsData?.name)
            } else {
                if (isFrom) {
                    setTitleText(context.getString(R.string.hint_range_from_date))
                } else {
                    setTitleText(context.getString(R.string.hint_range_to_date))
                }
            }
            setSelection(getSelectionDate())
            setCalendarConstraints(constraintsBuilder.build())
        }.build()

        datePicker.show(fragmentManager, DatePickerBuilder::class.java.simpleName)

        val timePicker = MaterialTimePicker
            .Builder()
            .setTitleText(fieldsData?.name)
            .build()

        var stringDateTime = ""
        datePicker.addOnPositiveButtonClickListener {
            val date = Date(it)
            if (fieldsData?.type == FieldType.DATETIME.value()) {
                stringDateTime = getFormatDate(date)
                timePicker.show(fragmentManager, DatePickerBuilder::class.java.name)
            } else if (fieldsData?.type == FieldType.DATE.value()) {
                onSuccess?.invoke(getFormatDate(date))
            }
        }

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val combinedDateTime = "$stringDateTime $hour:$minute"
            onSuccess?.invoke(combinedDateTime)
        }

        timePicker.addOnNegativeButtonClickListener {
            onFailure?.invoke()
        }

        timePicker.addOnCancelListener {
            onFailure?.invoke()
        }

        datePicker.addOnCancelListener {
            onFailure?.invoke()
        }
        datePicker.addOnNegativeButtonClickListener {
            onFailure?.invoke()
        }
    }

    private fun getValidators(fieldsData: FieldsData? = null): ArrayList<CalendarConstraints.DateValidator> {
        val validators: ArrayList<CalendarConstraints.DateValidator> = ArrayList()
        var endDate = MaterialDatePicker.todayInUtcMilliseconds()
        var requiredEndDate = false

        if (fieldsData != null) {
            fieldsData.minValue?.apply {
                this.getDateFromString(getFieldDateFormat(fieldsData))?.let { date ->
                    validators.add(DateValidatorPointForward.from(date.time))
                }
            }

            fieldsData.maxValue?.apply {
                this.getDateFromString(getFieldDateFormat(fieldsData))?.let { date ->
                    validators.add(DateValidatorPointBackward.before(date.time))
                }
            }
        } else {
            if (isFrom) {
                if (toDate.isNotEmpty()) {
                    toDate.getDateFromString()?.let { date ->
                        endDate = Date(date.time.plus(addOneDay)).time
                    }
                    requiredEndDate = true
                }
            } else {
                if (fromDate.isNotEmpty()) {
                    fromDate.getDateFromString()?.let { date ->
                        validators.add(DateValidatorPointForward.from(date.time))
                    }
                    requiredEndDate = !isFutureDate
                }
            }
            if (requiredEndDate) {
                validators.add(DateValidatorPointBackward.before(endDate))
            }
        }

        return validators
    }

    private fun getSelectionDate(): Long {
        var selectionDate = MaterialDatePicker.todayInUtcMilliseconds()
        if (isFrom) {
            if (fromDate.isNotEmpty()) {
                fromDate.getddMMyyyyStringDate()?.let { stringDate ->
                    selectionDate = getSelectionDate(stringDate)
                }
            }
        } else {
            if (toDate.isNotEmpty()) {
                toDate.getddMMyyyyStringDate()?.let { stringDate ->
                    selectionDate = getSelectionDate(stringDate)
                }
            }
        }
        return selectionDate
    }

    private fun getFormatDate(currentTime: Date): String {
        return SimpleDateFormat(dateFormat, Locale.ENGLISH).format(currentTime)
    }

    private fun String.getDateFromString(format: String = dateFormat): Date? {
        return SimpleDateFormat(format, Locale.ENGLISH).parse(this)
    }

    private fun String.getddMMyyyyStringDate(): String? {
        val formatter = SimpleDateFormat(dateFormatddMMyyyy, Locale.ENGLISH)
        val date = SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(this)
        if (date != null) {
            return formatter.format(date)
        }

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

    private fun getFieldDateFormat(fieldsData: FieldsData? = null): String {
        return when (fieldsData?.type) {
            FieldType.DATETIME.value() -> DATE_FORMAT_6
            FieldType.DATE.value() -> DATE_FORMAT_2_1
            else -> dateFormat
        }
    }
}
