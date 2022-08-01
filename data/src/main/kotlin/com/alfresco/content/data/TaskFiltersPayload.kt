package com.alfresco.content.data

/**
 * Marked as TaskFilters
 */
data class TaskFilters(
    val assignment: String = "involved",
    val sort: String = "created-desc",
    val start: Int = 0,
    val page: Int = 0,
    val state: String = "",
    val text: String = ""
) {
    companion object {
        /**
         * @param page
         * return active filter as TaskFilters obj
         */
        fun filter(page: Int = 0, state: String, sort: String): TaskFilters {
            return TaskFilters(
                page = page,
                state = state,
                sort = sort
            )
        }
    }
}

/**
 * Marked as TaskStateData class
 */
data class TaskStateData(
    val title: String = "Active",
    val assignment: String = "involved",
    val state: String = ""
)
