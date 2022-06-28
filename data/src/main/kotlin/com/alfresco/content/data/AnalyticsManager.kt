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
        params.putString(Parameters.EventName.value, EventName.SearchFacets.value)
        repository.logEvent(EventType.ActionEvent, params)
    }

    /**
     * analytics for multiple screen view
     */
    fun screenViewEvent(pageViewName: PageView, folderName: String = "", noOfFiles: Int = -1) {
        val params = repository.defaultParams()

        if (folderName.isNotEmpty())
            params.putString(Parameters.FolderName.value, folderName)

        if (noOfFiles > -1)
            params.putInt(Parameters.NumberOfFiles.value, noOfFiles)

        params.putString(Parameters.EventName.value, pageViewName.value)
        repository.logEvent(EventType.ScreenView, params)
    }
}
