package com.alfresco.content.data

class AnalyticsManager {

    val repository = AnalyticsRepository()

    fun previewFile(fileMimeType: String, fileExtension: String, success: Boolean) {
        println("analytics previewFile::  $fileMimeType : $fileExtension : $success")
        val params = repository.defaultParams()
        params.putString(Parameters.FileMimeType.value, fileMimeType)
        params.putString(Parameters.FileExtension.value, fileExtension)
        params.putBoolean(Parameters.Success.value, success)
        params.putString(Parameters.EventName.value, EventName.FilePreview.value)
        repository.logEvent(EventType.ActionEvent, params)
    }

    fun fileActionEvent(fileMimeType: String = "", fileExtension: String = "", eventName: EventName) {
        println("analytics fileActionEvent::  $fileMimeType : $fileExtension ")
        val params = repository.defaultParams()
        params.putString(Parameters.FileMimeType.value, fileMimeType)
        params.putString(Parameters.FileExtension.value, fileExtension)
        params.putString(Parameters.EventName.value, eventName.value)
        repository.logEvent(EventType.ActionEvent, params)
    }

    fun theme(name: String) {
        println("analytics theme::  $name  ")
        val params = repository.defaultParams()
        params.putString(Parameters.ThemeName.value, name)
        params.putString(Parameters.EventName.value, EventName.ChangeTheme.value)
        repository.logEvent(EventType.ActionEvent, params)
    }

    fun appLaunch() {
        println("analytics appLaunch  ")
        val params = repository.defaultParams()
        params.putString(Parameters.EventName.value, EventName.AppLaunched.value)
        repository.logEvent(EventType.ActionEvent, params)
    }

    fun searchFacets(name: String) {
        println("analytics searchFacets::  $name  ")
        val params = repository.defaultParams()
        params.putString(Parameters.FacetName.value, name)
        params.putString(Parameters.EventName.value, EventName.ChangeTheme.value)
        repository.logEvent(EventType.ActionEvent, params)
    }
}
