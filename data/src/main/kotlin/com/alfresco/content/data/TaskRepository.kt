package com.alfresco.content.data

import com.alfresco.content.data.payloads.CommentPayload
import com.alfresco.content.data.payloads.TaskFiltersPayload
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.process.apis.TaskAPI
import com.alfresco.process.models.RequestComment
import com.alfresco.process.models.RequestTaskFilters
import java.io.File

/**
 * Marked as TaskRepository class
 */
class TaskRepository(val session: Session = SessionManager.requireSession) {

    private val context get() = session.context
    val userEmail get() = session.account.email

    private val processService: TaskAPI by lazy {
        session.createProcessService(TaskAPI::class.java)
    }

    /**
     * execute the task list api and returns the response as ResponseList obj
     */
    suspend fun getTasks(filters: TaskFiltersPayload) = ResponseList.with(
        processService.taskList(
            includeFilters(filters)
        )
    )

    /**
     * returns the content uri to fetch the content data from server
     */
    fun contentUri(entry: Entry): String {
        return "https://mobileapps.envalfresco.com/activiti-app/app/rest/content/${entry.id}/raw"
    }

    fun getHttpClient() = session.getHttpClient()

    /**
     * execute the task details api and returns the response as TaskDataEntry obj
     */
    suspend fun getTaskDetails(taskID: String) = TaskEntry.with(
        processService.getTaskDetails(taskID)
    )

    /**
     * execute the get comments api and returns the response as ResponseComments obj
     */
    suspend fun getComments(taskID: String) = ResponseComments.with(
        processService.getComments(taskID)
    )

    /**
     * execute the get comments api and returns the response as ResponseComments obj
     */
    suspend fun addComments(taskID: String, payload: CommentPayload) = CommentEntry.with(
        processService.addComment(taskID, includeComment(payload))
    )

    /**
     * execute the get contents api and returns the response as ResponseContents obj
     */
    suspend fun getContents(taskID: String) = ResponseContents.with(
        processService.getContents(taskID)
    )

    private fun includeFilters(taskFilters: TaskFiltersPayload): RequestTaskFilters {
        return RequestTaskFilters(
            assignment = taskFilters.assignment,
            sort = taskFilters.sort,
            page = taskFilters.page,
            state = taskFilters.state,
            text = taskFilters.text,
            dueBefore = taskFilters.dueBefore,
            dueAfter = taskFilters.dueAfter
        )
    }

    private fun includeComment(payload: CommentPayload): RequestComment {
        return RequestComment(
            message = payload.message
        )
    }

    /**
     * return the content dir storage file to save the downloading content from server
     */
    fun getContentDirectory(fileName: String): File {
        return File(session.contentDir, fileName)
    }

    /**
     * Get TaskFilterDataModel from the internal storage or from assets
     */
    fun getTaskFiltersJSON(): TaskFiltersJson = getModelFromStringJSON(getJsonDataFromAsset(context, TASK_FILTERS_JSON) ?: "")
}
