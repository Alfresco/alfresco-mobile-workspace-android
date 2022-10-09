package com.alfresco.content.browse.tasks.list

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponseList
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.TaskFilterData
import com.alfresco.content.data.payloads.TaskFiltersPayload
import com.alfresco.content.listview.tasks.TaskListViewState
import com.alfresco.process.models.ProfileData

/**
 * Marked as TasksViewState class
 */
data class TasksViewState(
    val parent: Entry? = null,
    override val taskEntries: List<TaskEntry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponseList> = Uninitialized,
    val requestProfile: Async<ProfileData> = Uninitialized,
    val baseTaskEntries: List<TaskEntry> = emptyList(),
    val listSortDataChips: List<TaskFilterData> = emptyList(),
    val filterParams: TaskFiltersPayload = TaskFiltersPayload(),
    val loadItemsCount: Int = 0,
    val page: Int = 0
) : TaskListViewState {

    override val isCompact = false

    override fun copy(_entries: List<TaskEntry>) = copy(taskEntries = _entries)

    /**
     * update the latest response
     */
    fun update(
        response: ResponseList?
    ): TasksViewState {
        if (response == null) return this

        val totalLoadCount: Int

        val taskPageEntries = response.listTask

        val newTaskEntries = if (response.start != 0) {
            totalLoadCount = loadItemsCount.plus(response.size)
            baseTaskEntries + taskPageEntries
        } else {
            totalLoadCount = response.size
            taskPageEntries
        }

        return copy(
            taskEntries = newTaskEntries,
            baseTaskEntries = newTaskEntries,
            loadItemsCount = totalLoadCount,
            hasMoreItems = totalLoadCount < response.total
        )
    }
}
