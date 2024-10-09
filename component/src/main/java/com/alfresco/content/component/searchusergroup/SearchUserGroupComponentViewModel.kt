package com.alfresco.content.component.searchusergroup

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
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseUserGroupList
import com.alfresco.content.data.ReviewerType
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UserGroupDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

/**
 * Marked as SearchUserParams class
 */
data class SearchUserGroupParams(
    val nameOrIndividual: String = "",
    val emailOrGroups: String = "",
)

/**
 * Mark as SearchUserComponentViewModel class
 */
class SearchUserGroupComponentViewModel(
    val context: Context,
    stateChipCreate: SearchUserGroupComponentState,
    private val repository: TaskRepository,
) : MavericksViewModel<SearchUserGroupComponentState>(stateChipCreate) {
    private val liveSearchUserEvents: MutableStateFlow<SearchUserGroupParams>
    private val searchUserEvents: MutableStateFlow<SearchUserGroupParams>
    var params: SearchUserGroupParams
    var searchByNameOrIndividual = true
    var canSearchGroups = false

    init {
        params = SearchUserGroupParams()
        liveSearchUserEvents = MutableStateFlow(params)
        searchUserEvents = MutableStateFlow(params)

        setState {
            canSearchGroups = parent is ProcessEntry
            var listUserGroup: List<UserGroupDetails> = listOf()
            listUserGroup =
                when (parent) {
                    is ProcessEntry -> {
                        if (parent.reviewerType != ReviewerType.FUNCTIONAL_GROUP) {
                            listOf(getLoggedInUser())
                        } else {
                            listOf()
                        }
                    } else -> listOf(getLoggedInUser())
                }
            copy(listUserGroup = listUserGroup)
        }

        viewModelScope.launch {
            merge(
                liveSearchUserEvents.debounce(DEFAULT_DEBOUNCE_TIME),
                searchUserEvents,
            ).filter {
                it.nameOrIndividual.length >= MIN_QUERY_LENGTH || it.emailOrGroups.length >= MIN_QUERY_LENGTH
            }.executeOnLatest({
                if (canSearchGroups && it.emailOrGroups.isNotEmpty()) {
                    repository.searchGroups(it.emailOrGroups)
                } else {
                    repository.searchUser(it.nameOrIndividual, it.emailOrGroups)
                }
            }) {
                if (it is Loading) {
                    copy(requestUser = it)
                } else if (it is Fail) {
                    AnalyticsManager().apiTracker(APIEvent.SearchUser, false)
                    copy(requestUser = it)
                } else {
                    AnalyticsManager().apiTracker(APIEvent.SearchUser, true)
                    if (params.nameOrIndividual.isEmpty() && params.emailOrGroups.isEmpty()) {
                        updateUserGroupEntries(ResponseUserGroupList(), getLoggedInUser()).copy(requestUser = it)
                    } else {
                        updateUserGroupEntries(it(), getLoggedInUser()).copy(requestUser = it)
                    }
                }
            }
        }
    }

    private suspend fun <T, V> Flow<T>.executeOnLatest(
        action: suspend (value: T) -> V,
        stateReducer: SearchUserGroupComponentState.(Async<V>) -> SearchUserGroupComponentState,
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
        params =
            if (searchByNameOrIndividual) {
                if (term.isEmpty()) updateDefaultUserGroup(listOf(getLoggedInUser()))
                params.copy(nameOrIndividual = term, emailOrGroups = "")
            } else {
                if (canSearchGroups && term.isEmpty()) updateDefaultUserGroup(emptyList())
                params.copy(nameOrIndividual = "", emailOrGroups = term)
            }

        liveSearchUserEvents.value = params
    }

    private fun updateDefaultUserGroup(list: List<UserGroupDetails>) = setState { copy(listUserGroup = list) }

    private fun getLoggedInUser() = UserGroupDetails.with(repository.getAPSUser())

    companion object : MavericksViewModelFactory<SearchUserGroupComponentViewModel, SearchUserGroupComponentState> {
        const val MIN_QUERY_LENGTH = 1
        const val DEFAULT_DEBOUNCE_TIME = 300L

        override fun create(
            viewModelContext: ViewModelContext,
            state: SearchUserGroupComponentState,
        ) = SearchUserGroupComponentViewModel(viewModelContext.activity(), state, TaskRepository())
    }
}
