package com.alfresco.content.data

import com.alfresco.content.apis.RenditionsApi
import com.alfresco.content.models.Rendition
import com.alfresco.content.models.RenditionBodyCreate
import com.alfresco.content.models.RenditionEntry
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.delay

class RenditionRepository(val session: Session = SessionManager.requireSession) {

    private val service: RenditionsApi by lazy {
        session.createService(RenditionsApi::class.java)
    }

    suspend fun fetchRenditionUri(id: String): String? {
        val list = service.listRenditions(id, null).list?.entries ?: emptyList()
        val rendition = preferredRendition(list)
        return checkAndCreateRendition(rendition, id)
    }

    private fun preferredRendition(list: List<RenditionEntry>): RenditionEntry? {
        for (type in priorityList) {
            val rendition = list.find { it.entry.id == type }
            if (rendition != null) {
                return rendition
            }
        }
        return null
    }

    private suspend fun checkAndCreateRendition(rendition: RenditionEntry?, id: String): String? {
        val renditionId = rendition?.entry?.id ?: ""
        if (rendition != null) {
            if (rendition.entry.status == Rendition.StatusEnum.CREATED) {
                return renditionUri(id, renditionId)
            } else {
                service.createRendition(id, RenditionBodyCreate(renditionId))
                for (t in 0..MAX_TRIES) {
                    try {
                        val newRendition = service.getRendition(id, renditionId)
                        if (newRendition.entry.status == Rendition.StatusEnum.CREATED) {
                            return renditionUri(id, renditionId)
                        }
                    } catch (_: Exception) { }
                    delay(RETRY_DELAY)
                }
            }
        }
        return null
    }

    private fun renditionUri(id: String, renditionId: String): String {
        val baseUrl = SessionManager.currentSession?.baseUrl
        return "${baseUrl}alfresco/versions/1/nodes/$id/renditions/$renditionId/content?attachment=false&alf_ticket=${session.ticket}"
    }

    companion object {
        private const val MAX_TRIES = 10
        private const val RETRY_DELAY = 5000L
        private val priorityList = listOf("pdf", "imgpreview")
    }
}
