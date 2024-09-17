package com.alfresco.content.actions

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.FavoritesRepository
import com.alfresco.content.data.MenuActions
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.data.SearchRepository.Companion.SERVER_VERSION_NUMBER
import com.alfresco.content.data.Settings
import com.alfresco.coroutines.asFlow
import com.alfresco.events.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ContextualActionsViewModel(
    val state: ContextualActionsState,
    val context: Context,
    private val settings: Settings,
) : MavericksViewModel<ContextualActionsState>(state) {

    var listener: EntryListener? = null

    init {

        if (!state.isMultiSelection) {
            buildModelSingleSelection()
        } else {
            buildModelForMultiSelection()
        }

        // Update the model if necessary
        viewModelScope.on<ActionAddFavorite>(block = ::updateState)
        viewModelScope.on<ActionRemoveFavorite>(block = ::updateState)
        viewModelScope.on<ActionAddOffline>(block = ::updateState)
        viewModelScope.on<ActionRemoveOffline>(block = ::updateState)
        viewModelScope.on<ActionMoveFilesFolders>(block = ::updateState)
        viewModelScope.on<ActionUpdateFileFolder>(block = ::updateState)
        viewModelScope.on<ActionStartProcess>(block = ::updateState)
    }

    private fun buildModelSingleSelection() = withState { state ->
        // If entry is partial and not in the offline tab

        if (state.entries.isNotEmpty()) {
            state.entries.first().let { entry ->

                if (entry.isPartial && !entry.hasOfflineStatus) {
                    viewModelScope.launch {
                        fetchEntry(entry).execute {
                            when (it) {
                                is Success ->
                                    ContextualActionsState(entries = listOf(it()), actions = makeActions(it()), topActions = makeTopActions(it()), fetch = it)

                                is Fail ->
                                    ContextualActionsState(entries = listOf(entry), actions = makeActions(entry), topActions = makeTopActions(entry), fetch = it)

                                else ->
                                    copy(fetch = it)
                            }
                        }
                    }
                } else {
                    setState { copy(actions = makeActions(entry), topActions = makeTopActions(entry), fetch = Success(entry)) }
                }
            }
        }
    }

    private fun buildModelForMultiSelection() = withState { state ->
        // If entry is partial and not in the offline tab
        setState { copy(entries = state.entries, actions = makeMultiActions(state), topActions = emptyList()) }
    }

    private fun updateState(action: Action) {
        val entry = action.entry as Entry
        if (action is ActionStartProcess) {
            onStartProcess(action.entries.ifEmpty { listOf(entry) })
        }
        setState {
            ContextualActionsState(
                entries = if (isMultiSelection) action.entries else listOf(entry),
                actions = if (isMultiSelection) makeMultiActions(this) else makeActions(entry),
                topActions = makeTopActions(entry),
            )
        }
    }

    fun setEntryListener(listener: EntryListener) {
        this.listener = listener
    }

    private fun onStartProcess(entries: List<Entry>) = entries.run {
        if (entries.all { it.isFile }) {
            listener?.onProcessStart(entries)
        }
    }

    private fun fetchEntry(entry: Entry): Flow<Entry> =
        when (entry.type) {
            Entry.Type.SITE -> FavoritesRepository()::getFavoriteSite.asFlow(entry.id)
            else -> BrowseRepository()::fetchEntry.asFlow(entry.id)
        }

    fun execute(action: Action) =
        action.execute(context, GlobalScope)

    fun executeMulti(action: Action) =
        action.executeMulti(context, GlobalScope)

    private fun makeActions(entry: Entry): List<Action> =
        when {
            entry.isTrashed -> {
                actionsForTrashed(entry)
            }

            entry.hasOfflineStatus -> {
                actionsForOffline(entry)
            }

            else -> {
                defaultActionsFor(entry)
            }
        }

    fun makeMultiActions(state: ContextualActionsState): List<Action> {
        val actions = mutableListOf<Action>()
        val entry = Entry.withSelectedEntries(state.entries)

        when {
            state.entries.all { it.isTrashed } -> {
                // Added restore and delete actions
                actions.addAll(
                    listOfNotNull(
                        if (isMenuActionEnabled(MenuActions.Restore)) ActionRestore(entry, state.entries) else null,
                        if (isMenuActionEnabled(MenuActions.PermanentlyDelete)) ActionDeleteForever(entry, state.entries) else null,
                    ),
                )
            }

            state.entries.all { it.hasOfflineStatus } -> {
                // Added Offline action
                actions.addAll(offlineMultiActionFor(entry, state.entries))
            }

            else -> {
                // Added common actions
                actions.addAll(sharedActions(entry, state.entries))
            }
        }

        return actions
    }

    private fun sharedActions(entry: Entry, entries: List<Entry>): List<Action> {
        val actions = mutableListOf<Action>()
        // Added Favorite Action
        val version = SearchRepository().getPrefsServerVersion()

        val hasNonFavoriteEntries = entries.any { !it.isFavorite }

        val favouriteActions = when {
            version.toInt() < SERVER_VERSION_NUMBER -> null
            hasNonFavoriteEntries && isMenuActionEnabled(MenuActions.AddFavourite) -> ActionAddFavorite(entry, entries)
            !hasNonFavoriteEntries && isMenuActionEnabled(MenuActions.RemoveFavourite) -> ActionRemoveFavorite(entry, entries)
            else -> null
        }

        actions.addAll(listOfNotNull(favouriteActions))

        // Added Start Process Action
        actions.addAll(processMultiActionFor(entry, entries))

        // Added Move Action
        if (isMoveDeleteAllowed(entries)) {
            actions.addAll(
                listOfNotNull(
                    if (isMenuActionEnabled(MenuActions.Move)) ActionMoveFilesFolders(entry, entries) else null,
                ),
            )
        }

        // Added Offline Action
        actions.addAll(offlineMultiActionFor(entry, entries))

        // Added Delete Action
        if (isMoveDeleteAllowed(entries)) {
            actions.addAll(
                listOfNotNull(
                    if (isMenuActionEnabled(MenuActions.Trash)) ActionDelete(entry, entries) else null,
                ),
            )
        }
        return actions
    }

    private fun defaultActionsFor(entry: Entry) =
        listOf(
            externalActionsFor(entry),
            favoriteActionFor(entry),
            if (settings.isProcessEnabled && entry.isFile) actionsProcesses(entry) else listOf(),
            renameMoveActionFor(entry),
            offlineActionFor(entry),
            deleteActionFor(entry),
        ).flatten()

    private fun actionsForTrashed(entry: Entry): List<Action> =
        listOfNotNull(
            if (isMenuActionEnabled(MenuActions.Restore)) ActionRestore(entry) else null,
            if (isMenuActionEnabled(MenuActions.PermanentlyDelete)) ActionDeleteForever(entry) else null,
        )

    private fun actionsForOffline(entry: Entry): List<Action> =
        listOf(
            externalActionsFor(entry),
            if (settings.isProcessEnabled && entry.isFile) actionsProcesses(entry) else listOf(),
            offlineActionFor(entry),
        ).flatten()

    private fun actionsProcesses(entry: Entry): List<Action> =
        listOfNotNull(
            if (isMenuActionEnabled(MenuActions.StartProcess)) ActionStartProcess(entry) else null,
        )

    private fun processMultiActionFor(entry: Entry, entries: List<Entry>): List<Action> {
        return if (settings.isProcessEnabled && (entries.isNotEmpty() && entries.all { it.isFile })) {
            listOfNotNull(
                if (isMenuActionEnabled(MenuActions.StartProcess)) ActionStartProcess(entry, entries) else null,
            )
        } else {
            emptyList()
        }
    }

    private fun offlineActionFor(entry: Entry): List<Action> {
        if (!entry.isFile && !entry.isFolder || (entry.hasOfflineStatus && !entry.isOffline)) {
            return emptyList()
        }

        return listOfNotNull(
            when {
                entry.isOffline && isMenuActionEnabled(MenuActions.RemoveOffline) -> ActionRemoveOffline(entry)
                !entry.isOffline && isMenuActionEnabled(MenuActions.AddOffline) -> ActionAddOffline(entry)
                else -> null
            },
        )
    }

    private fun offlineMultiActionFor(entry: Entry, entries: List<Entry>): List<Action> {
        val filteredOffline = entries.filter { it.isFile || it.isFolder }.filter { !it.hasOfflineStatus || it.isOffline }

        return when {
            filteredOffline.any { !it.isOffline } -> {
                listOfNotNull(
                    if (isMenuActionEnabled(MenuActions.AddOffline)) ActionAddOffline(entry, entries) else null,
                )
            }

            else -> {
                listOfNotNull(
                    if (isMenuActionEnabled(MenuActions.RemoveOffline)) ActionRemoveOffline(entry, entries) else null,
                )
            }
        }
    }

    private fun favoriteActionFor(entry: Entry): List<Action> =
        listOfNotNull(
            when {
                entry.isFavorite && isMenuActionEnabled(MenuActions.RemoveFavourite) -> ActionRemoveFavorite(entry)
                !entry.isFavorite && isMenuActionEnabled(MenuActions.AddFavourite) -> ActionAddFavorite(entry)
                else -> null
            },
        )

    private fun externalActionsFor(entry: Entry): List<Action> =
        if (entry.isFile) {
            listOfNotNull(
                if (isMenuActionEnabled(MenuActions.OpenWith)) ActionOpenWith(entry) else null,
                if (isMenuActionEnabled(MenuActions.Download)) ActionDownload(entry) else null,
            )
        } else {
            emptyList()
        }

    private fun deleteActionFor(entry: Entry) = if (entry.canDelete) listOfNotNull(if (isMenuActionEnabled(MenuActions.Trash)) ActionDelete(entry) else null) else listOf()

    private fun renameMoveActionFor(entry: Entry): List<Action> {
        return if (entry.canDelete && (entry.isFile || entry.isFolder)) {
            listOfNotNull(
                if (isMenuActionEnabled(MenuActions.Rename)) ActionUpdateFileFolder(entry) else null,
                if (isMenuActionEnabled(MenuActions.Move)) ActionMoveFilesFolders(entry) else null,
            )
        } else {
            emptyList()
        }
    }

    private fun makeTopActions(entry: Entry): List<Action> =
        listOfNotNull(
            // Add or Remove favorite actions based on favorite status
            when {
                !entry.hasOfflineStatus -> when {
                    entry.isFavorite && isMenuActionEnabled(MenuActions.RemoveFavourite) -> ActionRemoveFavorite(entry)
                    !entry.isFavorite && isMenuActionEnabled(MenuActions.AddFavourite) -> ActionAddFavorite(entry)
                    else -> null
                }

                else -> null
            },
            // Download action if entry is a file
            if (entry.isFile && isMenuActionEnabled(MenuActions.Download)) ActionDownload(entry) else null,
        )

    private fun isMenuActionEnabled(menuActions: MenuActions): Boolean {
        if (state.appMenu?.isEmpty() == true) {
            return true
        }

        return state.appMenu?.find { it.id.lowercase() == menuActions.value.lowercase() }?.enabled == true
    }

    companion object : MavericksViewModelFactory<ContextualActionsViewModel, ContextualActionsState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ContextualActionsState,
        ) =
            // Requires activity context in order to present other fragments
            ContextualActionsViewModel(state, viewModelContext.activity, Settings(viewModelContext.activity))
    }
}
