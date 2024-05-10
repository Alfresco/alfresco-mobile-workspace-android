package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.TaskDataEntry
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.time.ZonedDateTime

/**
 * Marked as TaskEntry class
 */
@Parcelize
data class TaskEntry(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val assignee: UserGroupDetails? = null,
    var priority: Int = 0,
    var taskFormStatus: String? = null,
    val created: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val dueDate: ZonedDateTime? = null,
    val involvedPeople: List<UserGroupDetails> = listOf(),
    val formattedDueDate: String? = null,
    val duration: Long? = null,
    val isNewTaskCreated: Boolean = false,
    val processInstanceId: String? = null,
    val processDefinitionId: String? = null,
    val statusOption: List<OptionsModel> = emptyList(),
    val listContents: List<Entry> = emptyList(),
    val outcomes: List<OptionsModel> = emptyList(),
    val comment: String? = null,
    val processInstanceStartUserId: String? = null,
    val memberOfCandidateGroup: Boolean? = null,
) : ParentEntry(), Parcelable {

    val localDueDate: String?
        get() = formattedDueDate ?: dueDate?.toLocalDate()?.toString()

    companion object {

        /**
         * return the TaskEntry obj using TaskDataEntry
         */
        fun with(data: TaskDataEntry, apsUser: UserGroupDetails? = null, isNewTaskCreated: Boolean = false): TaskEntry {
            val isAssigneeUser = apsUser?.id == data.assignee?.id
            return TaskEntry(
                id = data.id ?: "",
                name = data.name ?: "",
                description = data.description,
                created = data.created,
                assignee = if (isAssigneeUser) apsUser?.let { UserGroupDetails.with(it) } else data.assignee?.let { UserGroupDetails.with(it) } ?: UserGroupDetails(),
                priority = data.priority?.toInt() ?: 0,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration,
                involvedPeople = data.involvedPeople?.map { UserGroupDetails.with(it) } ?: emptyList(),
                isNewTaskCreated = isNewTaskCreated,
                processInstanceId = data.processInstanceId,
                processDefinitionId = data.processDefinitionId,
                processInstanceStartUserId = data.processInstanceStartUserId,
                memberOfCandidateGroup = data.memberOfCandidateGroup,
                formattedDueDate = data.dueDate?.toLocalDate()?.toString(),
            )
        }

        /**
         * return the TaskEntry obj using ResponseListForm and existing TaskEntry
         */
        fun withTaskForm(response: ResponseListForm, parent: TaskEntry): TaskEntry {
            val formFields = response.fields.first().fields
            var description: String? = null
            var comment: String? = null
            var taskDueDate: String? = null
            var priority = -1
            var taskFormStatus: String? = null
            var listOptions: List<OptionsModel> = emptyList()
            var listContents: List<Entry> = emptyList()

            formFields.forEach {
                when (it.id.lowercase()) {
                    TaskFields.MESSAGE.value() -> {
                        description = it.value as? String
                    }

                    TaskFields.PRIORITY.value() -> {
                        priority = getPriorityNumValue(it.value as? String ?: "")
                    }

                    TaskFields.DUEDATE.value() -> {
                        taskDueDate = (it.value as? String)?.ifEmpty { null }
                    }

                    TaskFields.STATUS.value() -> {
                        it.apply {
                            taskFormStatus = value as? String
                            listOptions = options
                        }
                    }

                    TaskFields.COMMENT.value() -> {
                        comment = it.value as? String
                    }

                    TaskFields.ITEMS.value() -> {
                        listContents = (it.value as? List<*>)?.map { mapData -> gson.fromJson(JSONObject(mapData as Map<String, Entry>).toString(), Entry::class.java) } ?: emptyList()
                    }
                }
            }

            return TaskEntry(
                id = response.taskId ?: "",
                name = response.taskName ?: "",
                created = parent.created,
                description = description,
                priority = priority,
                taskFormStatus = taskFormStatus,
                statusOption = listOptions,
                listContents = listContents.map { Entry.withTaskForm(it) },
                formattedDueDate = taskDueDate,
                comment = comment,
                assignee = parent.assignee,
                endDate = parent.endDate,
                duration = parent.duration,
                processInstanceId = parent.processInstanceId,
                processDefinitionId = parent.processDefinitionId,
                outcomes = response.outcomes,
                processInstanceStartUserId = parent.processInstanceStartUserId,
                memberOfCandidateGroup = parent.memberOfCandidateGroup,
            )
        }

        /**
         * updating the task name and description into existing object
         */
        fun updateTaskNameDescription(
            data: TaskEntry,
            name: String,
            description: String,
        ): TaskEntry {
            return TaskEntry(
                id = data.id,
                name = name,
                description = description,
                created = data.created,
                assignee = data.assignee,
                priority = data.priority,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration,
                involvedPeople = data.involvedPeople,
                formattedDueDate = data.formattedDueDate,
            )
        }

        /**
         * updating the task due date into existing object
         */
        fun updateTaskDueDate(data: TaskEntry, formattedDueDate: String?, isClearDueDate: Boolean): TaskEntry {
            return TaskEntry(
                id = data.id,
                name = data.name,
                description = data.description,
                created = data.created,
                assignee = data.assignee,
                priority = data.priority,
                endDate = data.endDate,
                dueDate = if (isClearDueDate) null else data.dueDate,
                duration = data.duration,
                involvedPeople = data.involvedPeople,
                formattedDueDate = formattedDueDate,
            )
        }

        /**
         * updating the task priority into existing object
         */
        fun updateTaskPriority(data: TaskEntry, priority: Int): TaskEntry {
            return TaskEntry(
                id = data.id,
                name = data.name,
                description = data.description,
                created = data.created,
                assignee = data.assignee,
                priority = priority,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration,
                involvedPeople = data.involvedPeople,
                formattedDueDate = data.formattedDueDate,
            )
        }

        /**
         * updating the task assignee into existing object
         */
        fun updateAssignee(data: TaskEntry, assignee: UserGroupDetails): TaskEntry {
            return TaskEntry(
                id = data.id,
                name = data.name,
                created = data.created,
                assignee = assignee,
                priority = data.priority,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration,
                involvedPeople = data.involvedPeople,
                formattedDueDate = data.formattedDueDate,
            )
        }

        /**
         * updating the task status into existing object
         */
        fun updateTaskStatus(data: TaskEntry, status: String?): TaskEntry {
            return TaskEntry(
                id = data.id,
                name = data.name,
                description = data.description,
                created = data.created,
                assignee = data.assignee,
                priority = data.priority,
                taskFormStatus = status,
                statusOption = data.statusOption,
                listContents = data.listContents,
                formattedDueDate = data.formattedDueDate,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration,
                processInstanceStartUserId = data.processInstanceStartUserId,
                memberOfCandidateGroup = data.memberOfCandidateGroup,
                involvedPeople = data.involvedPeople,
            )
        }

        /**
         * update the status and comment and return the TaskEntry
         */
        fun updateTaskStatusAndComment(data: TaskEntry, status: String?, comment: String?): TaskEntry {
            return TaskEntry(
                id = data.id,
                name = data.name,
                description = data.description,
                created = data.created,
                assignee = data.assignee,
                priority = data.priority,
                taskFormStatus = status,
                statusOption = data.statusOption,
                listContents = data.listContents,
                comment = comment,
                formattedDueDate = data.formattedDueDate,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration,
                processInstanceStartUserId = data.processInstanceStartUserId,
                memberOfCandidateGroup = data.memberOfCandidateGroup,
                involvedPeople = data.involvedPeople,
            )
        }
    }
}

/**
 * Marked as TaskFields enum
 */
enum class TaskFields {

    MESSAGE,
    ITEMS,
    PRIORITY,
    DUEDATE,
    STATUS,
    TYPE,
    COMMENT,
    ;

    /**
     * returns value of enum in lowercase
     */
    fun value() = name.lowercase()
}
