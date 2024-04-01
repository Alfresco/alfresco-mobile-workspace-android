package com.alfresco.content.process.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R

@Composable
fun trailingIconColor() = MaterialTheme.colorScheme.onSurfaceVariant

fun Modifier.inputField() =
    this
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 12.dp) // Add padding or other modifiers as needed

fun integerInputError(value: String?, fieldsData: FieldsData, context: Context): Pair<Boolean, String> {
    var errorData = Pair(false, "")

    if (!value.isNullOrEmpty()) {
        val minValue = fieldsData.minValue?.toLong() ?: 0
        val maxValue = fieldsData.maxValue?.toLong() ?: 0

        if (value.toLong() < minValue) {
            errorData = Pair(true, context.getString(R.string.error_min_value, minValue))
        }

        if (value.toLong() > maxValue) {
            errorData = Pair(true, context.getString(R.string.error_max_value, maxValue))
        }
    }

    return errorData
}

fun singleLineInputError(value: String?, fieldsData: FieldsData, context: Context): Pair<Boolean, String> {
    var isError = false
    if (!value.isNullOrEmpty()) {
        isError = (value.length < fieldsData.minLength)
    }

    val errorMessage = if (isError) {
        context.getString(R.string.error_min_length, fieldsData.minLength)
    } else {
        ""
    }
    return Pair(isError, errorMessage)
}

fun multiLineInputError(value: String?, fieldsData: FieldsData, context: Context): Pair<Boolean, String> {
    var isError = false
    if (!value.isNullOrEmpty()) {
        isError = (value.length < fieldsData.minLength)
    }

    val errorMessage = if (isError) {
        context.getString(R.string.error_min_length, fieldsData.minLength)
    } else {
        ""
    }
    return Pair(isError, errorMessage)
}

fun booleanInputError(value: Boolean, fieldsData: FieldsData, context: Context): Pair<Boolean, String> {
    var isError = false
    if (fieldsData.required) {
        isError = !value
    }

    val errorMessage = if (isError) {
        context.getString(R.string.error_required_field)
    } else {
        ""
    }
    return Pair(isError, errorMessage)
}

fun amountInputError(value: String?, fieldsData: FieldsData, context: Context): Pair<Boolean, String> {
    val errorData = Pair(false, "")

    if (value.isNullOrEmpty()) {
        return errorData
    }

    if (value.toFloatOrNull() == null) {
        return Pair(true, context.getString(R.string.error_invalid_format))
    }

    val minValue = fieldsData.minValue?.toFloat() ?: 0f
    val maxValue = fieldsData.maxValue?.toFloat() ?: 0f

    if (value.toFloat() < minValue) {
        return Pair(true, context.getString(R.string.error_min_value, minValue.toInt()))
    }

    if (value.toFloat() > maxValue) {
        return Pair(true, context.getString(R.string.error_max_value, maxValue.toInt()))
    }

    return errorData
}

@SuppressLint("StringFormatInvalid")
fun dateTimeInputError(value: String?, fieldsData: FieldsData, context: Context): Pair<Boolean, String> {
    var isError = false

    if (!value.isNullOrEmpty()) {
        isError = (value.length < fieldsData.minLength)
    }

    val errorMessage = if (isError) {
        context.getString(R.string.error_required_field, fieldsData.minLength)
    } else {
        ""
    }
    return Pair(isError, errorMessage)
}

@SuppressLint("StringFormatInvalid")
fun dropDownRadioInputError(value: String?, fieldsData: FieldsData, context: Context): Pair<Boolean, String> {
    var isError = false

    if (!value.isNullOrEmpty()) {
        isError = (value.length < fieldsData.minLength)
    }

    val errorMessage = if (isError) {
        context.getString(R.string.error_required_field, fieldsData.minLength)
    } else {
        ""
    }
    return Pair(isError, errorMessage)
}

fun userGroupInputError(value: UserGroupDetails?, fieldsData: FieldsData, context: Context): Pair<Boolean, String> {
    val isError = (fieldsData.required && value == null)

    val errorMessage = ""

    return Pair(isError, errorMessage)
}
