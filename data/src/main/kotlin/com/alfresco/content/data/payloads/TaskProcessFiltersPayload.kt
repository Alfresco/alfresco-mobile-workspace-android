package com.alfresco.content.data.payloads

/**
 * Marked as TaskFiltersPayload
 */
data class TaskProcessFiltersPayload(
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
        fun updateFilters(obj: TaskProcessFiltersPayload, page: Int = 0): TaskProcessFiltersPayload {
            return TaskProcessFiltersPayload(
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

        /**
         *
         */
        fun updateFilters(obj: TaskProcessFiltersPayload, state: String, page: Int = 0): TaskProcessFiltersPayload {
            return TaskProcessFiltersPayload(
                assignment = obj.assignment,
                sort = obj.sort,
                start = obj.start,
                page = page,
                state = state.lowercase(),
                text = obj.text,
                dueBefore = obj.dueBefore,
                dueAfter = obj.dueAfter
            )
        }
    }
}

/**
 * Marked as ProcessFilters
 */
enum class ProcessFilters(val filter: String) {
    All("filter.option.all"),
    Running("running"),
    Active("filter.option.active"),
    Completed("filter.option.completed")
}
