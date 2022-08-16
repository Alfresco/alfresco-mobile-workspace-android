package com.alfresco.content.data.payloads

/**
 * Marked as TaskFiltersPayload
 */
data class TaskFiltersPayload(
    val assignment: String = "involved",
    val sort: String = "created-desc",
    var start: Int = 0,
    var page: Int = 0,
    var state: String = "",
    var text: String = "",
    var dueBefore: String = "",
    var dueAfter: String = ""
) {
    companion object {

        /**
         * update the filters and return the payload obj
         */
        fun updateFilters(obj: TaskFiltersPayload, page: Int = 0): TaskFiltersPayload {
            return TaskFiltersPayload(
                assignment = obj.assignment,
                sort = obj.sort,
                start = obj.start,
                page = page,
                state = obj.state,
                text = obj.text,
                dueBefore = obj.dueBefore,
                dueAfter = obj.dueAfter
            )
        }
    }
}
