package com.alfresco.content.data

/**
 * Marked as TaskFilters
 */
data class TaskFilters(
    val assignment: String = "",
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
                assignment = "assignee",
                page = 0,
                state = "open",
                text = ""
            )
        }

        /**
         * @param page
         * return active filter as TaskFilters obj
         */
        fun active(page: Int = 0): TaskFilters {
            return TaskFilters(
                assignment = "assignee",
                page = page,
                state = "open",
                text = ""
            )
        }

        /**
         * return default complete filter as TaskFilters obj
         */
        fun complete(): TaskFilters {
            return TaskFilters(
                assignment = "assignee",
                page = 0,
                state = "complete",
                text = ""
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
