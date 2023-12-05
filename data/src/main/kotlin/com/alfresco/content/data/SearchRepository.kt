package com.alfresco.content.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.DiscoveryService
import com.alfresco.content.apis.AdvanceSearchInclude
import com.alfresco.content.apis.FacetSearchInclude
import com.alfresco.content.apis.QueriesApi
import com.alfresco.content.apis.SearchApi
import com.alfresco.content.apis.SearchInclude
import com.alfresco.content.apis.advanceSearch
import com.alfresco.content.apis.recentFiles
import com.alfresco.content.apis.simpleSearch
import com.alfresco.content.models.AppConfigModel
import com.alfresco.content.models.RequestFacetField
import com.alfresco.content.models.RequestFacetIntervalsInIntervals
import com.alfresco.content.models.RequestFacetQueriesInner
import com.alfresco.content.models.RequestFacetSet
import com.alfresco.content.models.SetsItem
import com.alfresco.content.session.ActionSessionInvalid
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.session.SessionNotFoundException
import com.alfresco.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchRepository {

    lateinit var session: Session
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            session = SessionManager.requireSession
        } catch (e: SessionNotFoundException) {
            e.printStackTrace()
            coroutineScope.launch {
                EventBus.default.send(ActionSessionInvalid(true))
            }
        }
    }

    private val context get() = session.context

    private val searchService: SearchApi by lazy {
        session.createService(SearchApi::class.java)
    }

    private val offlineRepository: OfflineRepository by lazy {
        OfflineRepository()
    }

    private val queryService: QueriesApi by lazy {
        session.createService(QueriesApi::class.java)
    }

    suspend fun search(
        terms: String,
        nodeId: String?,
        filters: SearchFilters,
        advanceSearchFilters: AdvanceSearchFilters,
        searchFacetData: SearchFacetData,
        skipCount: Int,
        maxItems: Int,
    ) = if (filters.contains(SearchFilter.Libraries)) {
        ResponsePaging.with(
            queryService.findSites(
                terms,
                skipCount,
                maxItems,
            ),
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
                    includeFacetIntervalsFrom(searchFacetData.searchFacetIntervals),
                ),
                "V2",
            ),
        )
    } else {
        ResponsePaging.with(
            searchService.simpleSearch(
                terms,
                if (filters.contains(SearchFilter.Contextual)) nodeId else null,
                skipCount,
                maxItems,
                includeFrom(filters),
            ),
        )
    }

    suspend fun search(
        terms: String,
        nodeId: String?,
        filters: SearchFilters,
        skipCount: Int,
        maxItems: Int,
    ) =
        ResponsePaging.withExtension(
            searchService.simpleSearch(
                terms,
                if (filters.contains(SearchFilter.Contextual)) nodeId else null,
                skipCount,
                maxItems,
                includeFrom(filters),
            ),
        )

    /**
     * returns the ResponsePaging obj after filtering the data on the basis of files and folders
     */
    fun offlineSearch(name: String, listFacetFields: AdvanceSearchFilters): ResponsePaging {
        val folderSearchData = listFacetFields.find { it.query.contains("cm:folder") }
        val fileSearchData = listFacetFields.find { it.query.contains("cm:content") }
        val list = if (fileSearchData != null && folderSearchData != null) {
            offlineRepository.offlineSearch(name)
        } else if (folderSearchData != null) {
            offlineRepository.offlineSearch(name).filter { it.isFolder }
        } else if (fileSearchData != null) {
            offlineRepository.offlineSearch(name).filter { it.isFile }
        } else offlineRepository.offlineSearch(name)
        return ResponsePaging.with(list)
    }

    private fun getNodeID(advanceSearchFilters: AdvanceSearchFilters): Boolean {
        val isContextual = advanceSearchFilters.find {
            it.query.contains(SearchFilter.Contextual.name)
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
        if (!searchFacetFields.isNullOrEmpty()) {
            searchFacetFields.mapTo(mutableListOf()) {
                RequestFacetField(
                    label = it.label,
                    field = it.field,
                    mincount = it.mincount,
                )
            }
        } else {
            null
        }

    private fun includeFacetQueriesFrom(searchFacetQueries: SearchFacetQueries) =
        if (!searchFacetQueries.isNullOrEmpty()) {
            searchFacetQueries.mapTo(mutableListOf()) {
                RequestFacetQueriesInner(
                    label = it.label,
                    query = it.query,
                    group = it.group,
                )
            }
        } else {
            null
        }

    private fun includeFacetIntervalsFrom(searchFacetIntervals: SearchFacetIntervals) =
        if (!searchFacetIntervals.isNullOrEmpty()) {
            searchFacetIntervals.mapTo(mutableListOf()) {
                RequestFacetIntervalsInIntervals(
                    label = it.label,
                    field = it.field,
                    sets = includeFacetSetsFrom(it.sets),
                )
            }
        } else {
            null
        }

    private fun includeFacetSetsFrom(searchFacetSets: List<SetsItem>?) =
        if (!searchFacetSets.isNullOrEmpty()) {
            searchFacetSets.mapTo(mutableListOf()) {
                RequestFacetSet(
                    label = it.label,
                    start = it.start,
                    end = it.end,
                    startInclusive = it.startInclusive,
                    endInclusive = it.endInclusive,
                )
            }
        } else {
            null
        }

    suspend fun getRecents(skipCount: Int, maxItems: Int): ResponsePaging {
        val version = getServerVersion()

        if (version.toInt() >= SERVER_VERSION_NUMBER) {
            return ResponsePaging.with(
                searchService.recentFiles(
                    session.account.id,
                    MAX_RECENT_FILES_AGE,
                    skipCount,
                    maxItems,
                    true,
                ),
            )
        } else {
            return ResponsePaging.with(
                searchService.recentFiles(
                    session.account.id,
                    MAX_RECENT_FILES_AGE,
                    skipCount,
                    maxItems,
                ),
            )
        }
    }

    fun getPrefsServerVersion(): String {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPrefs.getString(SERVER_VERSION, "") ?: ""
    }

    private fun saveServerVersion(serverVersion: String) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.putString(SERVER_VERSION, serverVersion)
        editor.apply()
    }

    private suspend fun getServerVersion(): String {
        val version = getPrefsServerVersion()
        if (version.isNullOrEmpty()) {
            val acc = session.account

            val authConfig = AuthConfig.jsonDeserialize(acc.authConfig)

            authConfig?.let { config ->

                val discoveryService = DiscoveryService(context, config)

                val contentServiceDetailsObj = withContext(Dispatchers.IO) {
                    discoveryService.getContentServiceDetails(Uri.parse(acc.serverUrl).host ?: "")
                }

                val serverVersion = contentServiceDetailsObj?.version?.split(".")?.get(0) ?: ""
                saveServerVersion(serverVersion)
                return serverVersion
            }

            return ""
        } else {
            return version
        }
    }

    /**
     * Get AppConfigModel from the internal storage or from assets
     */
    fun getAppConfig(): AppConfigModel = getModelFromStringJSON(getJsonDataFromAsset(context, APP_CONFIG_JSON) ?: "")

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
        val onChange: () -> Unit,
    ) : SharedPreferences.OnSharedPreferenceChangeListener {

        init {
            PreferenceManager.getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?,
        ) {
            onChange()
        }
    }

    companion object {
        private const val RECENT_SEARCH_KEY = "recent_searches"
        private const val MAX_RECENT_FILES_AGE = 30
        private const val MAX_RECENT_SEARCHES = 15
        private const val SERVER_VERSION = "server_version"
        const val SERVER_VERSION_NUMBER = 23
    }
}
