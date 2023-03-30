package com.alfresco.content.browse.processes.list

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseList
import com.alfresco.content.data.payloads.TaskProcessFiltersPayload
import com.alfresco.content.listview.processes.ProcessListViewState

/**
 * Marked as TasksViewState class
 */
data class ProcessesViewState(
    val parent: ProcessEntry? = null,
    override val hasMoreItems: Boolean = false,
    override val processEntries: List<ProcessEntry> = emptyList(),
    override val request: Async<ResponseList> = Uninitialized,
    val baseTaskEntries: List<ProcessEntry> = emptyList(),
    val filterParams: TaskProcessFiltersPayload = TaskProcessFiltersPayload(),
    val loadItemsCount: Int = 0,
    val page: Int = 0
) : ProcessListViewState {

    override val isCompact = false

    override fun copy(_entries: List<ProcessEntry>) = copy(processEntries = _entries)

    /**
     * update the latest response
     */
    fun update(
        response: ResponseList?
    ): ProcessesViewState {
        if (response == null) return this

        val totalLoadCount: Int

        val taskPageEntries = response.listProcesses

        val newTaskEntries = if (response.start != 0) {
            totalLoadCount = loadItemsCount.plus(response.size)
            baseTaskEntries + taskPageEntries
        } else {
            totalLoadCount = response.size
            taskPageEntries
        }

        return copy(
            processEntries = newTaskEntries,
            baseTaskEntries = newTaskEntries,
            loadItemsCount = totalLoadCount,
            hasMoreItems = totalLoadCount < response.total
        )
    }
}
