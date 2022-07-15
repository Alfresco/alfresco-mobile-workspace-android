package com.alfresco.content.browse.tasks

import android.content.Context
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.browse.R
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponseList
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.Tasks
import com.alfresco.content.data.Tasks.Active
import com.alfresco.content.listview.tasks.TaskListViewState
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

/**
 * Marked as TasksViewState class
 */
data class TasksViewState(
    val parent: Entry? = null,
    override val taskEntries: List<TaskEntry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponseList> = Uninitialized,
    val baseTaskEntries: List<TaskEntry> = emptyList(),
    val displayTask: Tasks = Active,
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

        var totalLoadCount = 0

        val taskPageEntries = response.listTask

        val newTaskEntries = if (response.start != 0) {
            totalLoadCount = loadItemsCount.plus(response.size)
            baseTaskEntries + taskPageEntries
        } else {
            totalLoadCount = response.size
            taskPageEntries
        }

        return copyUpdatingEntries(newTaskEntries).copy(
            baseTaskEntries = taskPageEntries,
            loadItemsCount = totalLoadCount,
            hasMoreItems = totalLoadCount < response.total
        )
    }

    private fun copyUpdatingEntries(newEntries: List<TaskEntry>) =
        when (sortOrder) {
            SortOrder.ByModifiedDate -> groupByModifiedDateReducer(newEntries)
            else -> defaultReducer(newEntries)
        }

    /**
     * set sort order depends on the different views
     */
    val sortOrder: SortOrder
        get() = SortOrder.ByModifiedDate

    private fun defaultReducer(newEntries: List<TaskEntry>): TasksViewState =
        copy(
            taskEntries = newEntries
        )

    private fun groupByModifiedDateReducer(newEntries: List<TaskEntry>): TasksViewState {
        val now = ZonedDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay(now.zone)
        val startOfYesterday = startOfDay.minusDays(1)
        val firstOfWeek = startOfDay.with(ChronoField.DAY_OF_WEEK, 1)
        val firstOfLastWeek = firstOfWeek.minusWeeks(1)

        var currentGroup = ModifiedGroup.None
        val groupedList = mutableListOf<TaskEntry>()
        for (entry in newEntries) {
            val modified = entry.created ?: startOfDay

            val targetGroup = when {
                modified >= startOfDay -> ModifiedGroup.Today
                modified >= startOfYesterday -> ModifiedGroup.Yesterday
                modified >= firstOfWeek -> ModifiedGroup.ThisWeek
                modified >= firstOfLastWeek -> ModifiedGroup.LastWeek
                else -> ModifiedGroup.Older
            }

            if (currentGroup != targetGroup) {
                currentGroup = targetGroup

                groupedList.add(
                    TaskEntry(
                        id = currentGroup.title(),
                        type = TaskEntry.Type.GROUP,
                        name = currentGroup.title()
                    )
                )
            }
            groupedList.add(entry)
        }

        return copy(
            taskEntries = groupedList
        )
    }

    /**
     * Marked as ModifiedGroup enum class
     */
    enum class ModifiedGroup {
        Today,
        Yesterday,
        ThisWeek,
        LastWeek,
        Older,
        None;

        /**
         * set default value for title
         */
        fun title(): String {
            return valueMap[this] ?: ""
        }

        companion object {
            private val valueMap = HashMap<ModifiedGroup, String>()

            /**
             * fetching the sort title values from string.xml
             */
            fun prepare(context: Context) {
                valueMap[Today] = context.getString(R.string.modified_group_today)
                valueMap[Yesterday] = context.getString(R.string.modified_group_yesterday)
                valueMap[ThisWeek] = context.getString(R.string.modified_group_this_week)
                valueMap[LastWeek] = context.getString(R.string.modified_group_last_week)
                valueMap[Older] = context.getString(R.string.modified_group_older)
            }
        }
    }

    enum class SortOrder {
        ByModifiedDate,
        Default
    }
}
