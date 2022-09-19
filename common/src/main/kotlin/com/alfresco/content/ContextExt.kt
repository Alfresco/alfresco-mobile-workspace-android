package com.alfresco.content

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputEditText

inline fun <reified T : Context> Context.findBaseContext(): T? {
    var ctx: Context? = this
    do {
        if (ctx is T) {
            return ctx
        }
        if (ctx is ContextWrapper) {
            ctx = ctx.baseContext
        }
    } while (ctx != null)

    return null
}

fun FragmentActivity.hideSoftInput() {
    val imm: InputMethodManager? = getSystemService()
    val currentFocus = currentFocus
    if (currentFocus != null && imm != null) {
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}

fun Fragment.hideSoftInput() = requireActivity().hideSoftInput()

/**
 * show keyboard
 */
fun Context.showKeyboard(edt: TextInputEditText) {
    edt.requestFocus()
    val imm: InputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(edt, 0)
}

/**
 * @return the localised string from string.xml if found otherwise the same name
 */
fun Context.getLocalizedName(name: String): String {

    if (name.matches(".*\\d.*".toRegex()))
        return name

    val stringResource = resources.getIdentifier(name.lowercase(), "string", packageName)
    return if (stringResource != 0)
        getString(stringResource)
    else
        name
}
