package com.alfresco.content.browse

import android.content.Context
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.listview.ListViewState
import com.alfresco.kotlin.FilenameComparator
import com.alfresco.list.merge
import com.alfresco.list.replace
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

data class BrowseViewState(
    val path: String,
    val nodeId: String?,
    val parent: Entry? = null,
    override val entries: List<Entry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponsePaging> = Uninitialized,
    val baseEntries: List<Entry> = emptyList(),
    val uploads: List<Entry> = emptyList(),
    val uploadTransferList: List<Entry> = emptyList(),
    val totalTransfersSize: Int = 0
) : ListViewState {

    constructor(args: BrowseArgs) : this(args.path, args.id)

    override val isCompact: Boolean
        get() = when (path) {
            "site", "folder", "extension", "my-libraries", "fav-libraries" -> true
            else -> false
        }

    fun update(
        response: ResponsePaging?
    ): BrowseViewState {
        if (response == null) return this

        val nextPage = response.pagination.skipCount > 0
        val pageEntries = response.entries
        val newEntries = if (nextPage) {
            baseEntries + pageEntries
        } else {
            pageEntries
        }

        return copyIncludingUploads(newEntries, uploads, response.pagination.hasMoreItems)
    }

    fun updateUploads(uploads: List<Entry>): BrowseViewState {
        // Merge data only after at least the first page loaded
        // [parent] is a good enough flag for the initial load.
        return if (parent != null) {
            copyIncludingUploads(baseEntries, uploads, hasMoreItems)
        } else {
            copy(uploads = uploads)
        }
    }

    fun updateExtensionUploads(uploads: List<Entry>): BrowseViewState {
        return copy(uploadTransferList = uploads)
    }

    private fun copyIncludingUploads(
        entries: List<Entry>,
        uploads: List<Entry>,
        hasMoreItems: Boolean
    ): BrowseViewState {
        val mixedUploads = uploads.transformCompletedUploads()
        val mergedEntries = mergeInUploads(entries, mixedUploads, !hasMoreItems)
        val baseEntries = mergedEntries.filter { !it.isUpload }

        return copyUpdatingEntries(mergedEntries)
            .copy(
                baseEntries = baseEntries,
                uploads = uploads,
                hasMoreItems = hasMoreItems
            )
    }

    private fun mergeInUploads(base: List<Entry>, uploads: List<Entry>, includeRemaining: Boolean): List<Entry> {
        return merge(base, uploads, includeRemainingRight = includeRemaining) { left: Entry, right: Entry ->
            if (left.isFolder || right.isFolder) {
                val cmp = right.isFolder.compareTo(left.isFolder)
                if (cmp == 0) {
                    FilenameComparator.compare(left.name, right.name)
                } else {
                    cmp
                }
            } else {
                FilenameComparator.compare(left.name, right.name)
            }
        }.distinctBy { if (it.id.isEmpty()) it.boxId else it.id }
    }

    /*
     * Transforms completed uploads into network items, so further interaction with them
     * doesn't require special logic.
     */
    private fun List<Entry>.transformCompletedUploads(): List<Entry> =
        map {
            if (it.isUpload && it.isSynced) {
                // Marking as partial avoids needing to store allowableOperations
                it.copy(isUpload = false, offlineStatus = OfflineStatus.UNDEFINED, isPartial = true)
            } else {
                it
            }
        }

    private fun copyUpdatingEntries(newEntries: List<Entry>) =
        when (sortOrder) {
            SortOrder.ByModifiedDate -> groupByModifiedDateReducer(newEntries)
            else -> defaultReducer(newEntries)
        }

    val sortOrder: SortOrder
        get() = when (path) {
            "recents" -> SortOrder.ByModifiedDate // TODO:
            else -> SortOrder.Default
        }

    private fun defaultReducer(newEntries: List<Entry>): BrowseViewState =
        copy(
            entries = newEntries
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

                groupedList.add(
                    Entry(
                        currentGroup.title(),
                        null,
                        Entry.Type.GROUP,
                        currentGroup.title(),
                        null,
                        null
                    )
                )
            }
            groupedList.add(entry)
        }

        return copy(
            entries = groupedList
        )
    }

    override fun copy(_entries: List<Entry>): ListViewState = copy(entries = _entries)

    override fun copyRemoving(entry: Entry): ListViewState =
        copyIncludingUploads(
            baseEntries.filter { it.id != entry.id },
            uploads,
            hasMoreItems
        )

    override fun copyUpdating(entry: Entry): ListViewState =
        copyIncludingUploads(
            baseEntries.replace(entry) { it.id == entry.id },
            uploads,
            hasMoreItems
        )

    enum class ModifiedGroup {
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

    enum class SortOrder {
        ByModifiedDate,
        Default
    }
}
