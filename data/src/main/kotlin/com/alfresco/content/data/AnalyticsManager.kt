package com.alfresco.content.data

import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager

/**
 * Marked as AnalyticsManager class
 */
class AnalyticsManager(val session: Session = SessionManager.requireSession) {

    private val repository: AnalyticsRepository by lazy { AnalyticsRepository(session) }

    /**
     * analytics event for preview file
     */
    fun previewFile(fileMimeType: String, fileExtension: String, success: Boolean) {
        val params = repository.defaultParams()
        params.putString(Parameters.FileMimeType.value, fileMimeType)
        params.putString(Parameters.FileExtension.value, fileExtension)
        params.putBoolean(Parameters.Success.value, success)
        params.putString(Parameters.EventName.value, EventName.FilePreview.value.lowercase())
        repository.logEvent(EventName.FilePreview.value.lowercase(), params)
    }

    /**
     * analytics for multiple actions
     */
    fun fileActionEvent(fileMimeType: String = "", fileExtension: String = "", eventName: EventName) {
        val params = repository.defaultParams()
        params.putString(Parameters.FileMimeType.value, fileMimeType)
        params.putString(Parameters.FileExtension.value, fileExtension)
        params.putString(Parameters.EventName.value, eventName.value.lowercase())
        repository.logEvent(eventName.value.lowercase(), params)
    }

    /**
     * analytics for task filters
     */
    fun taskFiltersEvent(name: String) {
        val params = repository.defaultParams()
        val eventName = "event_${name.replace(" ","_")}"
        params.putString(Parameters.EventName.value, eventName.lowercase())
        repository.logEvent(eventName.lowercase(), params)
    }

    /**
     * analytics for task complete
     */
    fun taskEvent(eventName: EventName) {
        val params = repository.defaultParams()
        params.putString(Parameters.EventName.value, eventName.value.lowercase())
        repository.logEvent(eventName.value.lowercase(), params)
    }

    /**
     * analytics for theme change
     */
    fun theme(name: String) {
        val params = repository.defaultParams()
        params.putString(Parameters.ThemeName.value, name)
        params.putString(Parameters.EventName.value, EventName.ChangeTheme.value.lowercase())
        repository.logEvent(EventName.ChangeTheme.value.lowercase(), params)
    }

    /**
     * analytics for app launch
     */
    fun appLaunch() {
        val params = repository.defaultParams()
        params.putString(Parameters.EventName.value, EventName.AppLaunched.value.lowercase())
        repository.logEvent(EventName.AppLaunched.value.lowercase(), params)
    }

    /**
     * analytics for search facets
     */
    fun searchFacets(name: String) {
        val params = repository.defaultParams()
        params.putString(Parameters.FacetName.value, name)
        params.putString(Parameters.EventName.value, EventName.SearchFacets.value.lowercase())
        repository.logEvent(EventName.SearchFacets.value.lowercase(), params)
    }

    /**
     * analytics for multiple screen view
     */
    fun screenViewEvent(pageViewName: PageView, noOfFiles: Int = -1) {
        val params = repository.defaultParams()

        if (noOfFiles > -1) {
            params.putInt(Parameters.NumberOfFiles.value, noOfFiles)
        }

        params.putString(Parameters.EventName.value, pageViewName.value.lowercase())
        repository.logEvent(pageViewName.value.lowercase(), params)
    }

    /**
     * analytics for API tracker
     */
    fun apiTracker(apiName: APIEvent, status: Boolean = false, size: String = "") {
        val apiTrackName = if (status) "${apiName.value}_success".lowercase() else "${apiName.value}_fail".lowercase()

        val params = repository.defaultParams()
        params.putString(Parameters.EventName.value, apiTrackName)
        params.putBoolean(Parameters.Success.value, status)
        if (size.isNotEmpty()) {
            params.putString(Parameters.FileSize.value, size)
        }
        repository.logEvent(apiTrackName, params)
    }
}
