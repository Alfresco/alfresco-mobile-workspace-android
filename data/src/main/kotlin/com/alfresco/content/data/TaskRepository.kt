package com.alfresco.content.data

import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.process.apis.TaskAPI
import com.alfresco.process.models.RequestTaskFilters

/**
 * Marked as TaskRepository class
 */
class TaskRepository(val session: Session = SessionManager.requireSession) {

    private val context get() = session.context

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

    private fun includeFilters(taskFilters: TaskFiltersPayload): RequestTaskFilters {
        println("TaskRepository.includeFilters $taskFilters")
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

    /**
     * Get TaskFilterDataModel from the internal storage or from assets
     */
    fun getTaskFiltersJSON(): TaskFiltersJson = getModelFromStringJSON(getJsonDataFromAsset(context, TASK_FILTERS_JSON) ?: "")
}
