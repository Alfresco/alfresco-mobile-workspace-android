package com.alfresco.content

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Calls the specified [lambda] with the a [F] fragment as the receiver and returns its result.
 * Uses [F] fragment from the current fragment manager if available or creates one using [factory].
 */
suspend fun <F : Fragment, R> withFragment(
    context: Context,
    tag: String,
    lambda: suspend (F) -> R,
    factory: () -> F
): R =
    lambda(suspendCancellableCoroutine { continuation ->
        findFragmentAndResume(context, tag, continuation, factory)
    })

/**
 * Calls the specified [lambda] with the a [F] fragment as the receiver and returns its result.
 * creates [F] fragment new instance using [factory].
 */
suspend fun <F : Fragment, R> withNewFragment(
    context: Context,
    tag: String,
    lambda: suspend (F) -> R,
    factory: () -> F
): R =
    lambda(suspendCancellableCoroutine { continuation ->
        findFragment(context, tag, continuation, factory)
    })

private fun <F : Fragment> findFragmentAndResume(
    context: Context,
    tag: String,
    continuation: CancellableContinuation<F>,
    factory: () -> F
) {
    val fragmentManager = when (context) {
        is AppCompatActivity -> context.supportFragmentManager
        is Fragment -> context.childFragmentManager
        else -> null
    }

    if (fragmentManager == null) {
        continuation.cancel(ClassCastException())
        return
    }

    var fragment = fragmentManager.findFragmentByTag(tag)
    if (fragment != null) {
        @Suppress("UNCHECKED_CAST")
        continuation.resume((fragment as F), null)
    } else {
        fragment = factory()
        fragmentManager.beginTransaction().add(
            fragment,
            tag
        ).runOnCommit {
            continuation.resume(fragment, null)
        }.commit()
    }
}

/**
 * finding new instance of fragment
 */
private fun <F : Fragment> findFragment(
    context: Context,
    tag: String,
    continuation: CancellableContinuation<F>,
    factory: () -> F
) {
    val fragmentManager = when (context) {
        is AppCompatActivity -> context.supportFragmentManager
        is Fragment -> context.childFragmentManager
        else -> null
    }

    if (fragmentManager == null) {
        continuation.cancel(ClassCastException())
        return
    }

    val fragment = factory()
    fragmentManager.beginTransaction().add(
        fragment,
        tag
    ).runOnCommit {
        continuation.resume(fragment, null)
    }.commit()
}
