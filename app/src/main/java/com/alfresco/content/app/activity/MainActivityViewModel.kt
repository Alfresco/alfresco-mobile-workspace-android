package com.alfresco.content.app.activity

import android.content.Context
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.actions.ActionAddOffline
import com.alfresco.content.actions.ActionCaptureMedia
import com.alfresco.content.actions.ActionRemoveOffline
import com.alfresco.content.actions.ActionSyncNow
import com.alfresco.content.actions.ActionUploadMedia
import com.alfresco.content.browse.transfer.TransferSyncNow
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.AuthenticationRepository
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.PeopleRepository
import com.alfresco.content.data.SyncService
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.network.ConnectivityTracker
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.viewer.ViewerArgs.Companion.VALUE_REMOTE
import com.alfresco.content.viewer.ViewerArgs.Companion.VALUE_SHARE
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
) : MavericksState

class MainActivityViewModel(
    state: MainActivityState,
    val context: Context
) : MavericksViewModel<MainActivityState>(state), LifecycleObserver {

    private val processLifecycleOwner = ProcessLifecycleOwner.get()
    private var refreshTicketJob: Job? = null
    private var syncService: SyncService? = null
    var readPermission: Boolean = false
    private val _navigationMode = MutableLiveData<NavigationMode>()
    val navigationMode: LiveData<NavigationMode> get() = _navigationMode
    private var mode: String? = null
    private var isFolder: Boolean = false

    init {
        // Start a new session
        val session = SessionManager.newSession(context)
        if (session != null) {
            init(context, session)
        }
    }

    private fun init(context: Context, session: Session) {
        AnalyticsManager(session).appLaunch()
        session.onSignedOut {
            TaskRepository().clearAPSData()
            setState { copy(reLoginCount = reLoginCount + 1, requiresReLogin = true) }
        }

        // Receives current state on observe
        processLifecycleOwner.lifecycle.addObserver(this)

        // Update connectivity status
        viewModelScope.launch {
            ConnectivityTracker.networkAvailable.execute {
                    copy(isOnline = it() == true)
                }
        }

        // Cleanup unused db entries
        cleanupStorage(session)
        syncService = configureSync(context, viewModelScope)
    }

    private fun cleanupStorage(session: Session) {
        OfflineRepository(session).removeCompletedUploads()
    }

    private fun configureSync(context: Context, coroutineScope: CoroutineScope) = SyncService(context, coroutineScope).also { service ->
        coroutineScope.on<ActionAddOffline> { service.sync() }
        coroutineScope.on<ActionRemoveOffline> { service.sync() }
        coroutineScope.on<ActionCaptureMedia> { service.upload() }
        coroutineScope.on<ActionUploadMedia> { service.upload() }
        coroutineScope.on<ActionSyncNow> { service.syncNow(it.overrideNetwork) }
        coroutineScope.on<TransferSyncNow> { service.upload() }
    }

    val requiresLogin: Boolean
        get() = SessionManager.currentSession == null

    val profileIcon: Uri = PeopleRepository.myPicture()

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
                    mode?.let {
                        if (!isFolder) _navigationMode.value = NavigationMode.FILE
                        else _navigationMode.value = NavigationMode.FOLDER
                    }
                } catch (_: Exception) {
                    delay(60 * 1000L)
                }
            }
            syncService?.uploadIfNeeded()
            syncService?.syncIfNeeded()
        }
    }

    enum class NavigationMode {
        FOLDER, FILE, LOGIN, DEFAULT
    }

    fun handleDataIntent(mode: String?, isFolder: Boolean) {
        this.mode = mode
        this.isFolder = isFolder
        when (mode) {
            VALUE_SHARE -> {
                _navigationMode.value = NavigationMode.FILE
            }
            VALUE_REMOTE -> {
                if (requiresLogin) _navigationMode.value = NavigationMode.LOGIN
                else _navigationMode.value = NavigationMode.DEFAULT
            }
            else -> _navigationMode.value = NavigationMode.DEFAULT
        }
    }

    companion object : MavericksViewModelFactory<MainActivityViewModel, MainActivityState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: MainActivityState
        ) = MainActivityViewModel(state, viewModelContext.app())
    }
}
