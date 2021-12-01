package com.alfresco.content.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.alfresco.content.apis.AdvanceSearchInclude
import com.alfresco.content.apis.AppConfigApi
import com.alfresco.content.apis.FacetSearchInclude
import com.alfresco.content.apis.QueriesApi
import com.alfresco.content.apis.SearchApi
import com.alfresco.content.apis.SearchInclude
import com.alfresco.content.apis.advanceSearch
import com.alfresco.content.apis.recentFiles
import com.alfresco.content.apis.simpleSearch
import com.alfresco.content.models.AppConfigModel
import com.alfresco.content.models.RequestFacetField
import com.alfresco.content.models.RequestFacetIntervalsIntervals
import com.alfresco.content.models.RequestFacetQueriesInner
import com.alfresco.content.models.RequestFacetSet
import com.alfresco.content.models.SetsItem
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager

class SearchRepository(val session: Session = SessionManager.requireSession) {

    private val context get() = session.context

    private val searchService: SearchApi by lazy {
        session.createService(SearchApi::class.java)
    }

    private val queryService: QueriesApi by lazy {
        session.createService(QueriesApi::class.java)
    }

    private val appConfigService: AppConfigApi by lazy {
        session.createServiceConfig(AppConfigApi::class.java)
    }

    suspend fun search(
        terms: String,
        nodeId: String?,
        filters: SearchFilters,
        advanceSearchFilters: AdvanceSearchFilters,
        searchFacetData: SearchFacetData,
        skipCount: Int,
        maxItems: Int
    ) = if (filters.contains(SearchFilter.Libraries)) {
        ResponsePaging.with(
            queryService.findSites(
                terms,
                skipCount,
                maxItems
            )
        )
    } else if (advanceSearchFilters.isNotEmpty()) {
        ResponsePaging.with(
            searchService.advanceSearch(
                terms,
                if (getNodeID(advanceSearchFilters)) nodeId else null,
                skipCount,
                maxItems,
                includeFrom(advanceSearchFilters),
                FacetSearchInclude(
                    includeFacetFieldsFrom(searchFacetData.searchFacetFields),
                    includeFacetQueriesFrom(searchFacetData.searchFacetQueries),
                    includeFacetIntervalsFrom(searchFacetData.searchFacetIntervals)
                )
            )
        )
    } else {
        ResponsePaging.with(
            searchService.simpleSearch(
                terms,
                if (filters.contains(SearchFilter.Contextual)) nodeId else null,
                skipCount,
                maxItems,
                includeFrom(filters)
            )
        )
    }

    private fun getNodeID(advanceSearchFilters: AdvanceSearchFilters): Boolean {
        val isContextual = advanceSearchFilters.find {
            it.query == SearchFilter.Contextual.name
        }
        return isContextual != null
    }

    private fun includeFrom(filters: SearchFilters) =
        filters.mapNotNullTo(mutableSetOf()) {
            when (it) {
                SearchFilter.Files -> SearchInclude.Files
                SearchFilter.Folders -> SearchInclude.Folders
                else -> null
            }
        }

    private fun includeFrom(advanceSearchFilters: AdvanceSearchFilters): MutableSet<AdvanceSearchInclude> {

        val listFilter = advanceSearchFilters.filter { it.query != SearchFilter.Contextual.name }

        val advanceSet = listFilter.mapTo(mutableSetOf()) {
            AdvanceSearchInclude(name = it.name, query = it.query)
        }
        return advanceSet
    }

    private fun includeFacetFieldsFrom(searchFacetFields: SearchFacetFields) =
        if (!searchFacetFields.isNullOrEmpty()) searchFacetFields.mapTo(mutableListOf()) {
            RequestFacetField(
//                label = it.label,
                field = it.field,
                mincount = it.mincount
            )
        } else null

    private fun includeFacetQueriesFrom(searchFacetQueries: SearchFacetQueries) =
        if (!searchFacetQueries.isNullOrEmpty()) searchFacetQueries.mapTo(mutableListOf()) {
            RequestFacetQueriesInner(
                label = it.label,
                query = it.query
            )
        } else null

    private fun includeFacetIntervalsFrom(searchFacetIntervals: SearchFacetIntervals) =
        if (!searchFacetIntervals.isNullOrEmpty()) searchFacetIntervals.mapTo(mutableListOf()) {
            RequestFacetIntervalsIntervals(
                label = it.label,
                field = it.field,
                sets = includeFacetSetsFrom(it.sets)
            )
        } else null

    private fun includeFacetSetsFrom(searchFacetSets: List<SetsItem>?) =
        if (!searchFacetSets.isNullOrEmpty()) searchFacetSets.mapTo(mutableListOf()) {
            RequestFacetSet(
                label = it.label,
                start = it.start,
                end = it.end,
                startInclusive = it.startInclusive,
                endInclusive = it.endInclusive
            )
        } else null

    suspend fun getRecents(skipCount: Int, maxItems: Int) =
        ResponsePaging.with(
            searchService.recentFiles(
                SessionManager.requireSession.account.id,
                MAX_RECENT_FILES_AGE,
                skipCount,
                maxItems
            )
        )

    /**
     * Get AppConfigModel from the internal storage or from assets
     */
    fun getAppConfig(): AppConfigModel = if (isAppConfigExistOnLocal(context)) {
        val jsonFileString = retrieveJSONFromInternalDirectory(context)
        getModelFromStringJSON(jsonFileString)
    } else {
        val jsonFileString = getJsonDataFromAsset(context, APP_CONFIG_JSON) ?: ""
        getModelFromStringJSON(jsonFileString)
    }

    /**
     * @property launch
     * Fetch App config from server and save to internal directory
     */
    suspend fun fetchAndSaveAppConfig(launch: Boolean) {
        if (launch || isTimeToFetchConfig(getAppConfigLastFetchTime())) {
            saveAppConfigFetchTime()
            val configModel = appConfigService.getAppConfig()
            val jsonString = getJSONFromModel(configModel)
            saveJSONToInternalDirectory(context, jsonString)
        }
    }

    private fun getAppConfigLastFetchTime(): Long {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPrefs.getLong(APP_CONFIG_FETCH_TIME_KEY, 0L)
    }

    private fun saveAppConfigFetchTime() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        val editor = sharedPrefs.edit()
        editor.putLong(APP_CONFIG_FETCH_TIME_KEY, System.currentTimeMillis())
        editor.apply()
    }

    fun getRecentSearches(): List<String> {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPrefs.getStringList(RECENT_SEARCH_KEY).toMutableList()
    }

    fun saveSearch(query: String) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        var list = sharedPrefs.getStringList(RECENT_SEARCH_KEY).toMutableList()

        // At most [MAX_RECENT_SEARCHES] distinct values, with the latest added to top
        list.remove(query)
        list.add(0, query)
        list = list.subList(0, minOf(list.count(), MAX_RECENT_SEARCHES))

        val editor = sharedPrefs.edit()
        editor.putStringList(RECENT_SEARCH_KEY, list)
        editor.apply()
    }

    fun clearRecentSearch() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.remove(RECENT_SEARCH_KEY)
        editor.apply()
    }

    class RecentSearchesChangeListener(
        val context: Context,
        val onChange: () -> Unit
    ) : SharedPreferences.OnSharedPreferenceChangeListener {

        init {
            PreferenceManager.getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            onChange()
        }
    }

    private companion object {
        const val RECENT_SEARCH_KEY = "recent_searches"
        const val APP_CONFIG_FETCH_TIME_KEY = "app_config_fetch_time"
        const val MAX_RECENT_FILES_AGE = 30
        const val MAX_RECENT_SEARCHES = 15
    }
}
