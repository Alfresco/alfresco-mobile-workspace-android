package com.alfresco.content.data

data class TaskFilters(
    val assignment: String = "",
    val sort: String = "",
    val start: Int = 0,
    val state: String = "",
    val text: String = ""
) {
    companion object {

        fun all(): TaskFilters {
            return TaskFilters(
                assignment = "assignee",
                sort = "created-desc",
                start = 0,
                state = "open",
                text = ""
            )
        }

        fun active(): TaskFilters {
            return TaskFilters(
                assignment = "assignee",
                sort = "created-desc",
                start = 0,
                state = "open",
                text = ""
            )
        }

        fun complete(): TaskFilters {
            return TaskFilters(
                assignment = "assignee",
                sort = "created-desc",
                start = 0,
                state = "complete",
                text = ""
            )
        }

    }

}

enum class Tasks {
    All,
    Active,
    Completed

}


