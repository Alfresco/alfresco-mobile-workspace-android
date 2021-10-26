package com.alfresco.content

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

/**
 * show keyboard on TextInputEditText request
 */
fun TextInputEditText.showSoftInput(context: Context) {
    this.requestFocus()
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.toggleSoftInput(
        InputMethodManager.SHOW_FORCED,
        InputMethodManager.HIDE_IMPLICIT_ONLY
    )
}

fun Fragment.hideSoftInput() = requireActivity().hideSoftInput()

/**
 * @return the localised string from string.xml if found otherwise the same name
 */
fun Context.getLocalizedName(name: String): String {
    val stringResource = resources.getIdentifier(name.lowercase(), "string", packageName)
    return if (stringResource != 0)
        getString(stringResource)
    else
        name
}
