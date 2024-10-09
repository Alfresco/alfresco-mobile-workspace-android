package com.alfresco.content.data

import com.alfresco.content.apis.RenditionsApi
import com.alfresco.content.models.RenditionBodyCreate
import com.alfresco.content.models.RenditionEntry
import com.alfresco.content.session.ActionSessionInvalid
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.session.SessionNotFoundException
import com.alfresco.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.alfresco.content.models.Rendition as RenditionModel

class RenditionRepository {
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

    private val service: RenditionsApi by lazy {
        session.createService(RenditionsApi::class.java)
    }

    suspend fun fetchRendition(id: String): Rendition? {
        val list = service.listRenditions(id, null).list?.entries ?: emptyList()
        val rendition = preferredRendition(list)
        return checkAndCreateRendition(rendition, id)
    }

    private fun preferredRendition(list: List<RenditionEntry>): RenditionModel? {
        for (type in priorityList) {
            val rendition = list.find { it.entry.id == type }
            if (rendition != null) {
                return rendition.entry
            }
        }
        return null
    }

    private suspend fun checkAndCreateRendition(
        rendition: RenditionModel?,
        id: String,
    ): Rendition? {
        if (rendition == null) return null

        val renditionId = rendition.id
        val mimeType = rendition.content?.mimeType

        if (renditionId == null || mimeType == null) return null

        if (rendition.status == RenditionModel.StatusEnum.CREATED) {
            return Rendition(
                renditionUri(id, renditionId),
                mimeType,
            )
        } else {
            service.createRendition(id, RenditionBodyCreate(renditionId))
            for (t in 0..MAX_TRIES) {
                try {
                    val newRendition = service.getRendition(id, renditionId).entry
                    if (newRendition.status == RenditionModel.StatusEnum.CREATED) {
                        return Rendition(
                            renditionUri(id, renditionId),
                            mimeType,
                        )
                    }
                } catch (_: Exception) {
                }
                delay(RETRY_DELAY)
            }
        }
        return null
    }

    private fun renditionUri(
        id: String,
        renditionId: String,
    ) = "${session.baseUrl}alfresco/versions/1/nodes/$id/renditions/$renditionId/content" +
        "?attachment=false&alf_ticket=${session.ticket}"

    companion object {
        private const val MAX_TRIES = 10
        private const val RETRY_DELAY = 5000L
        private val priorityList = listOf("pdf", "imgpreview")
    }
}
