package com.alfresco.content.data

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.alfresco.content.data.payloads.CommentPayload
import com.alfresco.content.data.payloads.SystemPropertiesEntry
import com.alfresco.content.data.payloads.TaskProcessFiltersPayload
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.process.apis.ProcessAPI
import com.alfresco.process.apis.TaskAPI
import com.alfresco.process.models.AssignUserBody
import com.alfresco.process.models.ProfileData
import com.alfresco.process.models.RequestComment
import com.alfresco.process.models.RequestProcessInstances
import com.alfresco.process.models.RequestTaskFilters
import com.alfresco.process.models.TaskBodyCreate
import java.io.File
import java.net.URL
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Marked as TaskRepository class
 */
class TaskRepository(val session: Session = SessionManager.requireSession) {

    private val context get() = session.context
    val acsUserEmail get() = session.account.email

    private val tasksService: TaskAPI by lazy {
        session.createProcessService(TaskAPI::class.java)
    }

    private val processesService: ProcessAPI by lazy {
        session.createProcessService(ProcessAPI::class.java)
    }

    private val sharedPrefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * execute the task list api and returns the response as ResponseList obj
     */
    suspend fun getTasks(filters: TaskProcessFiltersPayload) = ResponseList.with(
        tasksService.taskList(
            includeTaskFilters(filters)
        ), getAPSUser()
    )

    /**
     * execute the task list api and returns the response as ResponseList obj
     */
    suspend fun getProcesses(filters: TaskProcessFiltersPayload) = ResponseList.with(
        processesService.processInstances(
            includeProcessFilters(filters)
        ), getAPSUser()
    )

    /**
     * returns the content uri to fetch the content data from server
     */
    fun contentUri(entry: Entry): String {
        val baseUrl = SessionManager.currentSession?.baseUrl
        return "${URL(baseUrl).protocol}://${URL(baseUrl).host}/activiti-app/app/rest/content/${entry.id}/raw"
    }

    /**
     * return the OkHttpClient obj which is getting from Session class
     */
    fun getHttpClient() = session.getHttpClient()

    /**
     * execute the task details api and returns the response as TaskDataEntry obj
     */
    suspend fun getTaskDetails(taskID: String) = TaskEntry.with(
        tasksService.getTaskDetails(taskID)
    )

    /**
     * execute the get comments api and returns the response as ResponseComments obj
     */
    suspend fun getComments(taskID: String) = ResponseComments.with(
        tasksService.getComments(taskID)
    )

    /**
     * execute the get comments api and returns the response as ResponseComments obj
     */
    suspend fun addComments(taskID: String, payload: CommentPayload) = CommentEntry.with(
        tasksService.addComment(taskID, includeComment(payload))
    )

    /**
     * execute the get contents api and returns the response as ResponseContents obj
     */
    suspend fun getContents(taskID: String) = ResponseContents.with(
        tasksService.getContents(taskID)
    )

    /**
     * execute the get complete task api and returns the response as Unit obj
     */
    suspend fun completeTask(taskID: String) = tasksService.completeTask(taskID)

    private fun includeTaskFilters(taskFilters: TaskProcessFiltersPayload): RequestTaskFilters {
        return RequestTaskFilters(
            assignment = taskFilters.assignment,
            sort = taskFilters.sort,
            page = taskFilters.page,
            state = taskFilters.state,
            text = taskFilters.text,
            dueBefore = taskFilters.dueBefore,
            dueAfter = taskFilters.dueAfter
        )
    }

    private fun includeProcessFilters(taskFilters: TaskProcessFiltersPayload): RequestProcessInstances {
        return RequestProcessInstances(
            sort = taskFilters.sort,
            state = taskFilters.state
        )
    }

    private fun includeComment(payload: CommentPayload): RequestComment {
        return RequestComment(
            message = payload.message
        )
    }

    /**
     * return the content dir storage file to save the downloading content from server
     */
    fun getContentDirectory(fileName: String): File {
        return File(session.contentDir, fileName)
    }

    /**
     * Get TaskFilterDataModel from the internal storage or from assets
     */
    fun getTaskFiltersJSON(): TaskFiltersJson = getModelFromStringJSON(getJsonDataFromAsset(context, TASK_FILTERS_JSON) ?: "")

    /**
     * executes the Api to fetch the user profile and save the local data in preferences
     */
    suspend fun getProcessUserProfile() = tasksService.getProfile()

    /**
     * If the ACS and APS users are same then it will return true otherwise false
     */
    fun isAcsAndApsSameUser(): Boolean {
        val acsUserEmail = session.account.email
        val processUserEmail = sharedPrefs.getString(KEY_PROCESS_USER_EMAIL, "")
        return acsUserEmail == processUserEmail
    }

    /**
     * It will save the APS user profile data on shared preferences.
     */
    fun saveProcessUserDetails(processUser: ProfileData) {
        val editor = sharedPrefs.edit()
        editor.putString(KEY_PROCESS_USER_ID, processUser.id?.toString() ?: "")
        editor.putString(KEY_PROCESS_USER_EMAIL, processUser.email ?: "")
        editor.putString(KEY_PROCESS_USER_FIRST_NAME, processUser.firstName ?: "")
        editor.putString(KEY_PROCESS_USER_LAST_NAME, processUser.lastName ?: "")
        editor.putString(KEY_PROCESS_USER_FULL_NAME, processUser.fullname ?: "")
        editor.apply()
    }

    /**
     * clear the process user detail from shared preferences
     */
    fun clearAPSData() {
        val editor = sharedPrefs.edit()
        editor.remove(KEY_PROCESS_USER_ID)
        editor.remove(KEY_PROCESS_USER_FIRST_NAME)
        editor.remove(KEY_PROCESS_USER_LAST_NAME)
        editor.remove(KEY_PROCESS_USER_EMAIL)
        editor.remove(KEY_PROCESS_USER_FULL_NAME)
        editor.apply()
    }

    /**
     * returns the userID of APS user
     */
    fun getAPSUser(): UserDetails {
        return UserDetails(
            id = sharedPrefs.getString(KEY_PROCESS_USER_ID, "0")?.toInt() ?: 0,
            email = sharedPrefs.getString(KEY_PROCESS_USER_EMAIL, "") ?: "",
            firstName = sharedPrefs.getString(KEY_PROCESS_USER_FIRST_NAME, "") ?: "",
            lastName = sharedPrefs.getString(KEY_PROCESS_USER_LAST_NAME, "") ?: ""
        )
    }

    /**
     * It will call the api to create the task and return the TaskEntry type obj
     */
    suspend fun createTask(name: String, description: String): TaskEntry {
        return TaskEntry.with(
            tasksService.createTask(TaskBodyCreate(name = name, description = description)), isNewTaskCreated = true
        )
    }

    /**
     * It will call the api to search the user by name or email and returns the ResponseUserList type obj
     */
    suspend fun searchUser(name: String, email: String): ResponseUserList {
        return ResponseUserList.with(
            tasksService.searchUser(filter = name, email = email)
        )
    }

    /**
     * It will call the api to update the task api and return the TaskEntry obj
     */
    suspend fun updateTaskDetails(taskEntry: TaskEntry): TaskEntry {
        return TaskEntry.with(
            tasksService.updateTask(
                taskEntry.id,
                TaskBodyCreate(
                    name = taskEntry.name,
                    description = taskEntry.description,
                    priority = taskEntry.priority.toString(),
                    dueDate = taskEntry.formattedDueDate ?: ""
                )
            ), isNewTaskCreated = true
        )
    }

    /**
     * It will call the api to update the task api and return the TaskEntry obj
     */
    suspend fun assignUser(taskID: String, assigneeID: String): TaskEntry {
        return TaskEntry.with(
            tasksService.assignUser(taskID, AssignUserBody(assignee = assigneeID))
        )
    }

    /**
     * It will call the api to delete the raw content and return the Response<Unit>
     */
    suspend fun deleteContent(contentId: String) = tasksService.deleteRawContent(contentId)

    /**
     * It will call the api to upload the raw content on process services.
     */
    suspend fun createEntry(local: Entry, file: File): Entry {
        // TODO: Support creating empty entries and folders
        requireNotNull(local.parentId)
        requireNotNull(local.mimeType)

        val filePart = file.asRequestBody(local.mimeType.toMediaTypeOrNull())
        val properties = mutableMapOf<String, RequestBody>()
        for ((k, v) in local.properties) {
            if (v.isNotEmpty()) {
                properties[k] = v.toRequestBody(MultipartBody.FORM)
            }
        }

        val multipartBody = MultipartBody.Part.createFormData("file", local.name, filePart)

        return Entry.with(
            tasksService.uploadRawContent(
                local.parentId,
                multipartBody
            ), local.parentId
        )
    }

    /**
     * It will call the system properties APIs on process services
     */
    suspend fun fetchAPSSystemProperties() = SystemPropertiesEntry.with(processesService.getSystemProperties())

    companion object {
        const val KEY_PROCESS_USER_ID = "process_user_id"
        const val KEY_PROCESS_USER_FULL_NAME = "process_user_full_name"
        const val KEY_PROCESS_USER_FIRST_NAME = "process_user_first_name"
        const val KEY_PROCESS_USER_LAST_NAME = "process_user_last_name"
        const val KEY_PROCESS_USER_EMAIL = "process_user_email"
    }
}
