package com.alfresco.content.data.payloads

/**
 * Marked as TaskFiltersPayload
 */
data class TaskProcessFiltersPayload(
    val processDefinitionId: String? = null,
    var assignment: String? = "involved",
    val sort: String = "created-desc",
    var start: Int = 0,
    var page: Int = 0,
    var size: Int = 25,
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

        /**
         * returns the default payload for the task list related to workflow
         */
        fun defaultTasksOfProcess(processDefinitionId: String? = null): TaskProcessFiltersPayload {
            return TaskProcessFiltersPayload(
                processDefinitionId = processDefinitionId,
                sort = "created-desc",
                assignment = null,
                size = 25,
                page = 0,
                state = "all"
            )
        }

        /**
         * update the task filters
         */
        fun updateTaskFilters(selectedStatus: String): TaskProcessFiltersPayload {
            val taskFilters = TaskProcessFiltersPayload()

            when (selectedStatus) {
                TaskStatus.All.name.lowercase(), TaskStatus.ACTIVE.name.lowercase(),
                TaskStatus.COMPLETED.name.lowercase() -> taskFilters.state = selectedStatus

                TaskStatus.CANDIDATE.name.lowercase() -> taskFilters.assignment = selectedStatus
            }

            return taskFilters
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

/**
 * Marked as TaskStatus
 */
enum class TaskStatus {
    All,
    ACTIVE,
    CANDIDATE,
    COMPLETED
}
