package com.alfresco.content.common

import java.util.regex.Pattern

/**
 * It will return true if the string matches the email pattern otherwise false.
 */
fun String.isValidEmail(): Boolean {
    val emailAddressPattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    return emailAddressPattern.matcher(this).matches()
}
