package com.alfresco.content.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SyncService(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var lastSyncTime: Long = 0L
    private var pendingSync: Job? = null
    private var scheduledSync: Job? = null

    /**
     * Executes sync filtering out extra calls within [TRIGGER_DELAY].
     */
    fun sync() {
        cancelPendingSync()
        cancelScheduledSync()
        pendingSync = scope.launch {
            delay(TRIGGER_DELAY)
            execute()
        }
    }

    /**
     * Executes sync without delay.
     */
    fun syncNow() {
        cancelPendingSync()
        cancelScheduledSync()
        execute()
    }

    /**
     * Schedules sync after [FOREGROUND_DELAY].
     */
    fun scheduleForegroundSync() {
        cancelScheduledSync()
        scheduledSync = scope.launch {
            delay(FOREGROUND_DELAY)
            syncOrWait()
        }
    }

    /**
     * Executes if at least [FOREGROUND_DELAY] has passed since last sync.
     */
    fun syncIfNeeded() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSyncTime > FOREGROUND_DELAY) {
            syncOrWait()
        }
    }

    /**
     * Executes sync if there's no pending sync job.
     */
    private fun syncOrWait() {
        pendingSync.let {
            if (it == null || it.isCompleted) {
                execute()
            }
        }
    }

    private fun execute() {
        lastSyncTime = System.currentTimeMillis()
        SyncWorker.syncNow(context)

        // Reschedule a timed sync
        scheduleForegroundSync()
    }

    private fun cancelScheduledSync() {
        scheduledSync?.cancel()
        scheduledSync = null
    }

    private fun cancelPendingSync() {
        pendingSync?.cancel()
        pendingSync = null
    }

    private companion object {
        private const val TRIGGER_DELAY = 10_000L
        private const val FOREGROUND_DELAY = 900_000L // 15 * 60
    }
}
