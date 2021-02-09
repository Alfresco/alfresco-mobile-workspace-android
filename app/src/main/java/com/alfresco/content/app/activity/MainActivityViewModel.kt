package com.alfresco.content.app.activity

import android.content.Context
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.actions.ActionAddOffline
import com.alfresco.content.actions.ActionRemoveOffline
import com.alfresco.content.actions.ActionSyncNow
import com.alfresco.content.data.AuthenticationRepository
import com.alfresco.content.data.PeopleRepository
import com.alfresco.content.data.SyncService
import com.alfresco.content.network.ConnectivityTracker
import com.alfresco.content.session.SessionManager
import com.alfresco.events.on
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MainActivityState(
    val reLoginCount: Int = 0, // new state on each invalid auth
    val requiresReLogin: Boolean = false,
    val isOnline: Boolean = true
) : MvRxState

class MainActivityViewModel(
    state: MainActivityState,
    context: Context
) : MvRxViewModel<MainActivityState>(state), LifecycleObserver {

    private val processLifecycleOwner = ProcessLifecycleOwner.get()
    private var refreshTicketJob: Job? = null
    private val syncService: SyncService

    init {
        // Start a new session
        val session = SessionManager.newSession(context)

        session?.onSignedOut {
            setState { copy(reLoginCount = reLoginCount + 1, requiresReLogin = true) }
        }

        // Receives current state on observe
        processLifecycleOwner.lifecycle.addObserver(this)

        // Update connectivity status
        ConnectivityTracker.startTracking(context)
        viewModelScope.launch {
            ConnectivityTracker
                .networkAvailable
                .execute {
                    copy(isOnline = it() == true)
                }
        }

        syncService = configureSync(context, viewModelScope)
    }

    private fun configureSync(context: Context, coroutineScope: CoroutineScope) =
        SyncService(context, coroutineScope).also { service ->
            coroutineScope.on<ActionAddOffline> { service.sync() }
            coroutineScope.on<ActionRemoveOffline> { service.sync() }
            coroutineScope.on<ActionSyncNow> { service.syncNow() }
        }

    val requiresLogin: Boolean
        get() = SessionManager.currentSession == null

    val profileIcon: Uri =
        PeopleRepository.myPicture()

    override fun onCleared() {
        super.onCleared()
        processLifecycleOwner.lifecycle.removeObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() = refreshTicket()

    private fun refreshTicket() {
        refreshTicketJob?.cancel()
        refreshTicketJob = viewModelScope.launch {
            var success = false
            while (!success && isActive) {
                try {
                    val session = SessionManager.currentSession ?: return@launch
                    session.ticket = AuthenticationRepository().fetchTicket()
                    success = true
                } catch (_: Exception) {
                    delay(60 * 1000L)
                }
            }
            syncService.syncIfNeeded()
        }
    }

    companion object : MvRxViewModelFactory<MainActivityViewModel, MainActivityState> {

        override fun create(viewModelContext: ViewModelContext, state: MainActivityState): MainActivityViewModel? {
            return MainActivityViewModel(state, viewModelContext.app())
        }
    }
}
