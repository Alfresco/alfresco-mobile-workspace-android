package com.alfresco.content.browse

import android.content.Context
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.Pagination
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.listview.ListViewState
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

data class BrowseViewState(
    val path: String,
    val nodeId: String?,
    override val entries: List<Entry> = emptyList(),
    override val lastPage: Pagination = Pagination.empty(),
    override val request: Async<ResponsePaging> = Uninitialized,
    val baseEntries: List<Entry> = emptyList()
) : ListViewState {

    constructor(args: BrowseArgs) : this(args.path, args.id)

    fun updateEntries(
        response: ResponsePaging?,
        sortOrder: Entry.SortOrder
    ): BrowseViewState {
        if (response == null) return this

        val nextPage = response.pagination.skipCount > 0
        val pageEntries = response.entries
        val newEntries = if (nextPage) { baseEntries + pageEntries } else { pageEntries }

        return when (sortOrder) {
            Entry.SortOrder.ByModifiedDate -> groupByModifiedDateReducer(newEntries)
            else -> baseReducer(newEntries)
        }.copy(lastPage = response.pagination)
    }

    private fun baseReducer(newEntries: List<Entry>): BrowseViewState {
        return copy(
            entries = newEntries,
            baseEntries = newEntries
        )
    }

    private fun groupByModifiedDateReducer(newEntries: List<Entry>): BrowseViewState {
        val now = ZonedDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay(now.zone)
        val startOfYesterday = startOfDay.minusDays(1)
        val firstOfWeek = startOfDay.with(ChronoField.DAY_OF_WEEK, 1)
        val firstOfLastWeek = firstOfWeek.minusWeeks(1)

        var currentGroup = ModifiedGroup.None
        val groupedList = mutableListOf<Entry>()
        for (entry in newEntries) {
            var targetGroup = ModifiedGroup.None
            val modified = entry.modified ?: startOfDay

            targetGroup = when {
                modified >= startOfDay -> ModifiedGroup.Today
                modified >= startOfYesterday -> ModifiedGroup.Yesterday
                modified >= firstOfWeek -> ModifiedGroup.ThisWeek
                modified >= firstOfLastWeek -> ModifiedGroup.LastWeek
                else -> ModifiedGroup.Older
            }

            if (currentGroup != targetGroup) {
                currentGroup = targetGroup

                groupedList.add(Entry(
                    currentGroup.title(),
                    Entry.Type.Group,
                    currentGroup.title(),
                    null,
                    null
                ))
            }
            groupedList.add(entry)
        }

        return copy(
            entries = groupedList,
            baseEntries = newEntries
        )
    }

    enum class ModifiedGroup() {
        Today,
        Yesterday,
        ThisWeek,
        LastWeek,
        Older,
        None;

        fun title(): String {
            return valueMap[this] ?: ""
        }

        companion object {
            private val valueMap = HashMap<ModifiedGroup, String>()

            fun prepare(context: Context) {
                valueMap[Today] = context.getString(R.string.modified_group_today)
                valueMap[Yesterday] = context.getString(R.string.modified_group_yesterday)
                valueMap[ThisWeek] = context.getString(R.string.modified_group_this_week)
                valueMap[LastWeek] = context.getString(R.string.modified_group_last_week)
                valueMap[Older] = context.getString(R.string.modified_group_older)
            }
        }
    }
}
