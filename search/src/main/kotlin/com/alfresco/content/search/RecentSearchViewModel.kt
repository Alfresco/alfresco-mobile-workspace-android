package com.alfresco.content.search

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.getStringList

data class RecentSearchViewState(
    val entries: List<String> = emptyList()
) : MvRxState

class RecentSearchViewModel(
    viewState: RecentSearchViewState,
    val context: Context
) : MvRxViewModel<RecentSearchViewState>(viewState),
    SharedPreferences.OnSharedPreferenceChangeListener {

    init {
        refresh()

        PreferenceManager.getDefaultSharedPreferences(context)
            .registerOnSharedPreferenceChangeListener(this)
    }

    private fun refresh() {
        val key = context.getString(R.string.recent_searches_key)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val list = sharedPrefs.getStringList(key).toMutableList()
        setState { copy(entries = list) }
    }

    companion object : MvRxViewModelFactory<RecentSearchViewModel, RecentSearchViewState> {
        override fun create(viewModelContext: ViewModelContext, viewState: RecentSearchViewState): RecentSearchViewModel? {
            return RecentSearchViewModel(viewState, viewModelContext.app())
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            context.getString(R.string.recent_searches_key) -> refresh()
        }
    }
}
