package com.alfresco.content.search.components

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal typealias DatePickerOnSuccess = (String) -> Unit
internal typealias DatePickerOnFailure = () -> Unit

data class DatePickerBuilder(
    val context: Context,
    var onSuccess: DatePickerOnSuccess? = null,
    var onFailure: DatePickerOnFailure? = null
) {

    fun onSuccess(callback: DatePickerOnSuccess?) =
        apply { this.onSuccess = callback }

    fun onFailure(callback: DatePickerOnFailure?) =
        apply { this.onFailure = callback }

    fun show() {
        val fragmentManager = when (context) {
            is AppCompatActivity -> context.supportFragmentManager
            is Fragment -> context.childFragmentManager
            else -> throw IllegalArgumentException()
        }

        val datePicker = MaterialDatePicker.Builder.datePicker().apply {
            setTitleText("Select date")
        }.build()

        datePicker.show(fragmentManager, DatePickerBuilder::class.java.simpleName)

        datePicker.addOnPositiveButtonClickListener {
            val date = Date(it)
            val stringDate = getFormatDate(date)
            println("selected date $stringDate")
            onSuccess?.invoke(stringDate)
        }
        datePicker.addOnNegativeButtonClickListener {
        }
        datePicker.addOnCancelListener {
            // Respond to cancel button click.
        }
        datePicker.addOnDismissListener {
            // Respond to dismiss events.
        }
    }

    private fun getFormatDate(currentTime: Date): String {
        val timeZoneDate = SimpleDateFormat("dd-MMM-yy", Locale.getDefault(Locale.Category.DISPLAY))
        return timeZoneDate.format(currentTime)
    }
}
