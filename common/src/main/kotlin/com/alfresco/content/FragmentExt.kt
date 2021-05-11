package com.alfresco.content

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine

internal suspend fun <F: Fragment, R> withFragment(
    context: Context,
    tag: String,
    lambda: suspend (F) -> R,
    factory: () -> F
): R =
    lambda(suspendCancellableCoroutine { continuation ->
        findPermissionFragment(context, tag, continuation, factory)
    })

private fun <F: Fragment> findPermissionFragment(
    context: Context,
    tag: String,
    continuation: CancellableContinuation<F>,
    factory: () -> F
) {
    val fragmentManager = when (context) {
        is AppCompatActivity -> context.supportFragmentManager
        is Fragment -> context.childFragmentManager
        else -> throw CancellationException("Context needs to be either AppCompatActivity or Fragment", ClassCastException())
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
