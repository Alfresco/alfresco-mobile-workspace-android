package com.alfresco.content.data

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.alfresco.content.data.payloads.CommentPayload
import com.alfresco.content.data.payloads.TaskFiltersPayload
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.process.apis.TaskAPI
import com.alfresco.process.models.ProfileData
import com.alfresco.process.models.RequestComment
import com.alfresco.process.models.RequestTaskFilters
import com.alfresco.process.models.TaskBodyCreate
import java.io.File

/**
 * Marked as TaskRepository class
 */
class TaskRepository(val session: Session = SessionManager.requireSession) {

    private val context get() = session.context
    val userEmail get() = session.account.email

    private val processService: TaskAPI by lazy {
        session.createProcessService(TaskAPI::class.java)
    }

    private val sharedPrefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * execute the task list api and returns the response as ResponseList obj
     */
    suspend fun getTasks(filters: TaskFiltersPayload) = ResponseList.with(
        processService.taskList(
            includeFilters(filters)
        )
    )

    /**
     * returns the content uri to fetch the content data from server
     */
    fun contentUri(entry: Entry): String {
        return "https://mobileapps.envalfresco.com/activiti-app/app/rest/content/${entry.id}/raw"
    }

    /**
     * return the OkHttpClient obj which is getting from Session class
     */
    fun getHttpClient() = session.getHttpClient()

    /**
     * execute the task details api and returns the response as TaskDataEntry obj
     */
    suspend fun getTaskDetails(taskID: String) = TaskEntry.with(
        processService.getTaskDetails(taskID)
    )

    /**
     * execute the get comments api and returns the response as ResponseComments obj
     */
    suspend fun getComments(taskID: String) = ResponseComments.with(
        processService.getComments(taskID)
    )

    /**
     * execute the get comments api and returns the response as ResponseComments obj
     */
    suspend fun addComments(taskID: String, payload: CommentPayload) = CommentEntry.with(
        processService.addComment(taskID, includeComment(payload))
    )

    /**
     * execute the get contents api and returns the response as ResponseContents obj
     */
    suspend fun getContents(taskID: String) = ResponseContents.with(
        processService.getContents(taskID)
    )

    /**
     * execute the get complete task api and returns the response as Unit obj
     */
    suspend fun completeTask(taskID: String) = processService.completeTask(taskID)

    private fun includeFilters(taskFilters: TaskFiltersPayload): RequestTaskFilters {
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
    suspend fun getProcessUserProfile() {
        val acsUserEmail = session.account.email
        val processUserEmail = sharedPrefs.getString(KEY_PROCESS_USER_EMAIL, "")
        if (acsUserEmail != processUserEmail) {
            val processUser = processService.getProfile()
            saveProcessUserDetails(processUser)
        }
    }

    private fun saveProcessUserDetails(processUser: ProfileData) {
        val editor = sharedPrefs.edit()
        editor.putString(KEY_PROCESS_USER_ID, processUser.id?.toString() ?: "")
        editor.putString(KEY_PROCESS_USER_EMAIL, processUser.email ?: "")
        editor.putString(KEY_PROCESS_USER_FULL_NAME, processUser.fullname ?: "")
        editor.apply()
    }

    /**
     * returns the userID of APS user
     */
    fun getProcessUserId(): String {
        return sharedPrefs.getString(KEY_PROCESS_USER_ID, "") ?: ""
    }

    /**
     * It will call the api to create the task and return the TaskEntry type obj
     */
    suspend fun createTask(name: String, description: String): TaskEntry {
        return TaskEntry.with(
            processService.createTask(TaskBodyCreate(name = name, description = description)), true
        )
    }

    /**
     * It will call the api to search the user by name or email and returns the ResponseUserList type obj
     */
    suspend fun searchUser(name: String, email: String): ResponseUserList {
        return ResponseUserList.with(
            processService.searchUser(filter = name, email = email)
        )
    }

    companion object {
        const val KEY_PROCESS_USER_ID = "process_user_id"
        const val KEY_PROCESS_USER_FULL_NAME = "process_user_full_name"
        const val KEY_PROCESS_USER_EMAIL = "process_user_email"
    }
}
