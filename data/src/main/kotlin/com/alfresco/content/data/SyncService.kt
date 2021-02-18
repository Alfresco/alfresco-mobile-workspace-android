package com.alfresco.content.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SyncService(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var lastSyncTime: Long = 0L
    private var pendingSync: Job? = null
    private var scheduledSync: Job? = null

    private val workManager = WorkManager.getInstance(context)
    private var latestWorkInfo: WorkInfo? = null

    init {
        scope.launch {
            observeWork().collect {
                latestWorkInfo = it
            }
        }
    }

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
    fun syncNow(overrideNetwork: Boolean) {
        cancelPendingSync()
        cancelScheduledSync()
        execute(overrideNetwork)
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

    private fun execute(overrideNetwork: Boolean = false) {
        lastSyncTime = System.currentTimeMillis()
        scheduleWork(overrideNetwork)

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

    private fun scheduleWork(overrideNetwork: Boolean) {
        val networkType = if (Settings(context).canSyncOverMeteredNetwork || overrideNetwork) {
            NetworkType.CONNECTED
        } else {
            NetworkType.UNMETERED
        }

        val policy = if (latestWorkInfo?.state == WorkInfo.State.RUNNING) {
            ExistingWorkPolicy.APPEND
        } else {
            // Existing work may start before scheduling new work causing a cancellation
            ExistingWorkPolicy.REPLACE
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager
            .beginUniqueWork(UNIQUE_WORK_NAME, policy, request)
            .enqueue()
    }

    private fun observeWork(): Flow<WorkInfo?> =
        workManager
            .getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME)
            .asFlow()
            .map { list ->
                list?.find { it.state == WorkInfo.State.RUNNING }
                ?: list?.find { !it.state.isFinished }
                ?: list?.firstOrNull()
            }

    companion object {
        private const val TRIGGER_DELAY = 10_000L
        private const val FOREGROUND_DELAY = 900_000L // 15 * 60
        private const val UNIQUE_WORK_NAME = "sync"

        fun observe(context: Context): Flow<SyncState?> =
            WorkManager
                .getInstance(context)
                .getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME)
                .asFlow()
                .map { list ->
                    list?.find { it.state == WorkInfo.State.RUNNING }
                    ?: list?.find { !it.state.isFinished }
                    ?: list?.firstOrNull()
                }
                .map {
                    it?.let { SyncState.from(it.state) }
                }

        // TODO: race condition work is cancelled but sync service may still trigger it
        fun cancel(context: Context) {
            WorkManager
                .getInstance(context)
                .cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }

    enum class SyncState {
        Enqueued,
        Running,
        Blocked,
        Finished;

        companion object {
            fun from(workInfoState: WorkInfo.State?): SyncState =
                when (workInfoState) {
                    WorkInfo.State.ENQUEUED -> Enqueued
                    WorkInfo.State.RUNNING -> Running
                    WorkInfo.State.BLOCKED -> Blocked
                    else -> Finished
                }
        }
    }
}
