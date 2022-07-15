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
         * return default all filter as TaskFilters obj
         */
        fun all(): TaskFilters {
            return TaskFilters(
                page = 0,
                state = "open"
            )
        }

        /**
         * @param page
         * return active filter as TaskFilters obj
         */
        fun active(page: Int = 0): TaskFilters {
            return TaskFilters(
                page = page,
                state = "open"
            )
        }

        /**
         * return default complete filter as TaskFilters obj
         */
        fun complete(): TaskFilters {
            return TaskFilters(
                page = 0,
                state = "complete"
            )
        }
    }
}

/**
 * Marked as Tasks class
 */
enum class Tasks {
    All,
    Active,
    Completed
}
