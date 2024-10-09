package com.alfresco

import timber.log.Timber
import java.util.regex.Pattern

object Logger {
    fun init(debugMode: Boolean) {
        if (debugMode) {
            Timber.plant(DebugTree())
        }
    }

    fun v(
        message: String,
        vararg args: Any?,
    ) {
        Timber.v(message, *args)
    }

    fun v(
        t: Throwable,
        message: String,
        vararg args: Any?,
    ) {
        Timber.v(t, message, *args)
    }

    fun v(t: Throwable) {
        Timber.v(t)
    }

    fun d(
        message: String,
        vararg args: Any?,
    ) {
        Timber.d(message, *args)
    }

    fun d(
        t: Throwable,
        message: String,
        vararg args: Any?,
    ) {
        Timber.d(t, message, *args)
    }

    fun d(t: Throwable) {
        Timber.d(t)
    }

    fun i(
        message: String,
        vararg args: Any?,
    ) {
        Timber.i(message, *args)
    }

    fun i(
        t: Throwable,
        message: String,
        vararg args: Any?,
    ) {
        Timber.i(t, message, *args)
    }

    fun i(t: Throwable) {
        Timber.i(t)
    }

    fun w(
        message: String,
        vararg args: Any?,
    ) {
        Timber.w(message, *args)
    }

    fun w(
        t: Throwable,
        message: String,
        vararg args: Any?,
    ) {
        Timber.w(t, message, *args)
    }

    fun w(t: Throwable) {
        Timber.w(t)
    }

    fun e(
        message: String,
        vararg args: Any?,
    ) {
        Timber.e(message, *args)
    }

    fun e(
        t: Throwable,
        message: String,
        vararg args: Any?,
    ) {
        Timber.e(t, message, *args)
    }

    fun e(t: Throwable) {
        Timber.e(t)
    }

    fun wtf(
        message: String,
        vararg args: Any?,
    ) {
        Timber.wtf(message, *args)
    }

    fun wtf(
        t: Throwable,
        message: String,
        vararg args: Any?,
    ) {
        Timber.wtf(t, message, *args)
    }

    fun wtf(t: Throwable) {
        Timber.wtf(t)
    }
}

/**
 * DebugTree tailored for Timber being wrapped within another class.
 */
private class DebugTree : Timber.DebugTree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        super.log(priority, createClassTag(), message, t)
    }

    private fun createClassTag(): String {
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size <= CALL_STACK_INDEX) {
            throw IllegalStateException("Synthetic stacktrace didn't have enough elements: are you using proguard?")
        }
        var tag = stackTrace[CALL_STACK_INDEX].className
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        tag = tag.substring(tag.lastIndexOf('.') + 1)
        return tag
    }

    companion object {
        private const val CALL_STACK_INDEX = 7
        private val ANONYMOUS_CLASS by lazy { Pattern.compile("(\\$\\d+)+$") }
    }
}
