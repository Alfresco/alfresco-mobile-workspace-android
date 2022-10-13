package com.alfresco.content.component

import android.content.Context
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.APIEvent
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.ResponseUserList
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UserDetails
import java.util.concurrent.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * Marked as SearchUserParams class
 */
data class SearchUserParams(
    val name: String = "",
    val email: String = ""
)

/**
 * Mark as SearchUserComponentViewModel class
 */
class SearchUserComponentViewModel(
    val context: Context,
    stateChipCreate: SearchUserComponentState,
    private val repository: TaskRepository
) : MavericksViewModel<SearchUserComponentState>(stateChipCreate) {
    private val liveSearchUserEvents: MutableStateFlow<SearchUserParams>
    private val searchUserEvents: MutableStateFlow<SearchUserParams>
    private var params: SearchUserParams
    var searchByName = true

    init {
        params = SearchUserParams()
        liveSearchUserEvents = MutableStateFlow(params)
        searchUserEvents = MutableStateFlow(params)

        setState {
            copy(listUser = listOf(getLoggedInUser()))
        }

        viewModelScope.launch {
            merge(
                liveSearchUserEvents.debounce(DEFAULT_DEBOUNCE_TIME),
                searchUserEvents
            ).filter {
                it.name.length >= MIN_QUERY_LENGTH || it.email.length >= MIN_QUERY_LENGTH
            }.executeOnLatest({
                repository.searchUser(it.name, it.email)
            }) {
                if (it is Loading) {
                    copy(requestUser = it)
                } else if (it is Fail) {
                    AnalyticsManager().apiTracker(APIEvent.SearchUser, false)
                    copy(requestUser = it)
                } else {
                    AnalyticsManager().apiTracker(APIEvent.SearchUser, true)
                    if (params.name.isEmpty() && params.email.isEmpty())
                        updateUserEntries(ResponseUserList(), getLoggedInUser()).copy(requestUser = it)
                    else {
                        updateUserEntries(it(), getLoggedInUser()).copy(requestUser = it)
                    }
                }
            }
        }
    }

    private suspend fun <T, V> Flow<T>.executeOnLatest(
        action: suspend (value: T) -> V,
        stateReducer: SearchUserComponentState.(Async<V>) -> SearchUserComponentState
    ) {
        collectLatest {
            setState { stateReducer(Loading()) }
            try {
                val result = action(it)
                setState { stateReducer(Success(result)) }
            } catch (e: CancellationException) {
                // No-op
            } catch (e: Throwable) {
                setState { stateReducer(Fail(e)) }
            }
        }
    }

    /**
     * update the params on the basis of name or email to search the user.
     */
    fun setSearchQuery(term: String) {
        params = if (searchByName)
            params.copy(name = term, email = "")
        else params.copy(name = "", email = term)

        liveSearchUserEvents.value = params
    }

    private fun getLoggedInUser() = UserDetails.with(repository.getAPSUser())

    companion object : MavericksViewModelFactory<SearchUserComponentViewModel, SearchUserComponentState> {
        const val MIN_QUERY_LENGTH = 1
        const val DEFAULT_DEBOUNCE_TIME = 300L
        override fun create(
            viewModelContext: ViewModelContext,
            state: SearchUserComponentState
        ) = SearchUserComponentViewModel(viewModelContext.activity(), state, TaskRepository())
    }
}
