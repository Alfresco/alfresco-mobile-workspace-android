package com.alfresco.content.data

/**
 * Marked as AnalyticsManager class
 */
class AnalyticsManager {

    val repository = AnalyticsRepository()

    /**
     * analytics event for preview file
     */
    fun previewFile(fileMimeType: String, fileExtension: String, success: Boolean) {
        val params = repository.defaultParams()
        params.putString(Parameters.FileMimeType.value, fileMimeType)
        params.putString(Parameters.FileExtension.value, fileExtension)
        params.putBoolean(Parameters.Success.value, success)
        params.putString(Parameters.EventName.value, EventName.FilePreview.value)
        repository.logEvent(EventType.ActionEvent, params)
    }

    /**
     * analytics for multiple actions
     */
    fun fileActionEvent(fileMimeType: String = "", fileExtension: String = "", eventName: EventName) {
        val params = repository.defaultParams()
        params.putString(Parameters.FileMimeType.value, fileMimeType)
        params.putString(Parameters.FileExtension.value, fileExtension)
        params.putString(Parameters.EventName.value, eventName.value)
        repository.logEvent(EventType.ActionEvent, params)
    }

    /**
     * analytics for theme change
     */
    fun theme(name: String) {
        val params = repository.defaultParams()
        params.putString(Parameters.ThemeName.value, name)
        params.putString(Parameters.EventName.value, EventName.ChangeTheme.value)
        repository.logEvent(EventType.ActionEvent, params)
    }

    /**
     * analytics for app launch
     */
    fun appLaunch() {
        val params = repository.defaultParams()
        params.putString(Parameters.EventName.value, EventName.AppLaunched.value)
        repository.logEvent(EventType.ActionEvent, params)
    }

    /**
     * analytics for search facets
     */
    fun searchFacets(name: String) {
        val params = repository.defaultParams()
        params.putString(Parameters.FacetName.value, name)
        params.putString(Parameters.EventName.value, EventName.ChangeTheme.value)
        repository.logEvent(EventType.ActionEvent, params)
    }
}
