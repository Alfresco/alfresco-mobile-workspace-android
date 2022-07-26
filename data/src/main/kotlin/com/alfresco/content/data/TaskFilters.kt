package com.alfresco.content.data

/**
 * Marked as TaskFilters
 */
data class TaskFilters(
    val assignment: String = "assignee",
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
        fun filter(page: Int = 0, state: String, sort: String, assignment: String = "assignee"): TaskFilters {
            return TaskFilters(
                assignment = assignment,
                page = page,
                state = state,
                sort = sort
            )
        }
    }
}

/**
 * Marked as Tasks class
 */
enum class TaskState {
    All,
    Active,
    Completed
}
