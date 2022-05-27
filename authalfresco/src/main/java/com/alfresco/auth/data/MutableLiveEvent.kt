package com.alfresco.auth.data

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A lifecycle-aware observable that sends only new updates after subscription.
 *
 * This avoids a common problem with events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to setValue() or call().
 *
 * Note that only one observer is going to be notified of changes.
 */
abstract class LiveEvent<T> : LiveData<T>()

/**
 * [LiveEvent] which publicly exposes [setValue] and [postValue] method.
 */
class MutableLiveEvent<T> : LiveEvent<T>() {
    private val mPending = AtomicBoolean(false)

    /**
     * Similar to [LiveData.observe] adds the given [observer] to the observers list within the
     * lifespan of the given [owner]. The events are dispatched on the main thread. Data will only
     * be delivered to the observer only on explicit [setValue]/[postValue].
     */
    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }

        // Observe the internal MutableLiveData
        super.observe(owner, { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    /**
     * Similar to [LiveData.setValue] but ensures data is only delivered during an explicit call.
     */
    @MainThread
    public override fun setValue(value: T?) {
        mPending.set(true)
        super.setValue(value)
    }

    /**
     * Similar to [LiveData.postValue].
     */
    public override fun postValue(value: T?) {
        super.postValue(value)
    }

    /**
     * Used for cases where [T] is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }

    private companion object {
        const val TAG = "MutableLiveEvent"
    }
}
