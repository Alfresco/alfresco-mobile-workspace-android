package com.alfresco.content.browse

import android.content.Context
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.listview.ListViewState
import com.alfresco.list.replace
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

data class BrowseViewState(
    val path: String,
    val nodeId: String?,
    override val entries: List<Entry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponsePaging> = Uninitialized,
    val baseEntries: List<Entry> = emptyList()
) : ListViewState {

    constructor(args: BrowseArgs) : this(args.path, args.id)

    fun update(
        response: ResponsePaging?
    ): BrowseViewState {
        if (response == null) return this

        val nextPage = response.pagination.skipCount > 0
        val pageEntries = response.entries
        val newEntries = if (nextPage) { baseEntries + pageEntries } else { pageEntries }

        return copyUpdatingBase(newEntries).copy(hasMoreItems = response.pagination.hasMoreItems)
    }

    private fun copyUpdatingBase(newEntries: List<Entry>) =
        when (sortOrder) {
            Entry.SortOrder.ByModifiedDate -> groupByModifiedDateReducer(newEntries)
            else -> baseReducer(newEntries)
        }

    val sortOrder: Entry.SortOrder
        get() = when (path) {
            "recents" -> Entry.SortOrder.ByModifiedDate // TODO:
            else -> Entry.SortOrder.Default
        }

    private fun baseReducer(newEntries: List<Entry>): BrowseViewState =
        copy(
            entries = newEntries,
            baseEntries = newEntries
        )

    private fun groupByModifiedDateReducer(newEntries: List<Entry>): BrowseViewState {
        val now = ZonedDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay(now.zone)
        val startOfYesterday = startOfDay.minusDays(1)
        val firstOfWeek = startOfDay.with(ChronoField.DAY_OF_WEEK, 1)
        val firstOfLastWeek = firstOfWeek.minusWeeks(1)

        var currentGroup = ModifiedGroup.None
        val groupedList = mutableListOf<Entry>()
        for (entry in newEntries) {
            val modified = entry.modified ?: startOfDay

            val targetGroup = when {
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

    override fun copy(_entries: List<Entry>): ListViewState = copy(entries = _entries)

    override fun copyRemoving(entry: Entry): ListViewState =
        copyUpdatingBase(baseEntries.filter { it.id != entry.id })

    override fun copyUpdating(entry: Entry): ListViewState =
        copyUpdatingBase(entries.replace(entry) {
            it.id == entry.id
        })

    fun copyPrepending(entry: Entry): BrowseViewState =
        copyUpdatingBase(listOf(entry) + baseEntries)

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
