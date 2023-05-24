package com.alfresco.content.data

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.alfresco.content.data.Settings.Companion.IS_PROCESS_ENABLED_KEY
import com.alfresco.content.data.payloads.CommentPayload
import com.alfresco.content.data.payloads.LinkContentPayload
import com.alfresco.content.data.payloads.SystemPropertiesEntry
import com.alfresco.content.data.payloads.TaskProcessFiltersPayload
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.process.apis.ProcessAPI
import com.alfresco.process.apis.TaskAPI
import com.alfresco.process.models.AssignUserBody
import com.alfresco.process.models.GroupInfo
import com.alfresco.process.models.PriorityModel
import com.alfresco.process.models.ProfileData
import com.alfresco.process.models.RequestComment
import com.alfresco.process.models.RequestLinkContent
import com.alfresco.process.models.RequestProcessInstances
import com.alfresco.process.models.RequestProcessInstancesQuery
import com.alfresco.process.models.RequestTaskFilters
import com.alfresco.process.models.TaskBodyCreate
import com.alfresco.process.models.UserInfo
import com.alfresco.process.models.ValuesModel
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
        processesService.processInstancesQuery(
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

    private fun includeProcessFilters(taskFilters: TaskProcessFiltersPayload): RequestProcessInstancesQuery {
        return RequestProcessInstancesQuery(
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
     * executes the Api to fetch the account Info
     */
    suspend fun getProcessAccountInfo() = processesService.accountInfo()

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
        editor.remove(IS_PROCESS_ENABLED_KEY)
        editor.apply()
    }

    /**
     * returns the userID of APS user
     */
    fun getAPSUser(): UserGroupDetails {
        return UserGroupDetails(
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
    suspend fun searchUser(name: String, email: String): ResponseUserGroupList {
        return ResponseUserGroupList.with(
            tasksService.searchUser(filter = name, email = email)
        )
    }

    /**
     * It will call the api to search the group by name and returns the ResponseUserList type obj
     */
    suspend fun searchGroups(name: String): ResponseUserGroupList {
        return ResponseUserGroupList.with(
            processesService.searchGroups(latest = name)
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
    suspend fun createEntry(local: Entry, file: File, uploadServerType: UploadServerType): Entry {
        // TODO: Support creating empty entries and folders
        requireNotNull(local.mimeType)
        requireNotNull(local.parentId)

        val filePart = file.asRequestBody(local.mimeType.toMediaTypeOrNull())
        val properties = mutableMapOf<String, RequestBody>()
        for ((k, v) in local.properties) {
            if (v.isNotEmpty()) {
                properties[k] = v.toRequestBody(MultipartBody.FORM)
            }
        }

        val multipartBody = MultipartBody.Part.createFormData("file", local.name, filePart)

        return when (uploadServerType) {
            UploadServerType.UPLOAD_TO_TASK -> {
                Entry.with(
                    tasksService.uploadRawContent(
                        local.parentId,
                        multipartBody
                    ), local.parentId, uploadServerType
                )
            }

            UploadServerType.UPLOAD_TO_PROCESS -> Entry.with(
                processesService.uploadRawContent(
                    multipartBody
                ), local.parentId, uploadServer = uploadServerType
            )

            else -> Entry()
        }
    }

    /**
     * It will call the system properties APIs on process services
     */
    suspend fun fetchAPSSystemProperties() = SystemPropertiesEntry.with(processesService.getSystemProperties())

    /**
     * It will execute the api to link ADW content with Processes
     */
    suspend fun linkADWContentToProcess(linkContentPayload: LinkContentPayload) =
        Entry.with(
            processesService.linkContentToProcess(
                includeLinkContent(linkContentPayload)
            ), uploadServer = UploadServerType.NONE
        )

    private fun includeLinkContent(payload: LinkContentPayload): RequestLinkContent {
        return RequestLinkContent(
            source = payload.source,
            sourceId = payload.sourceId,
            mimeType = payload.mimeType,
            name = payload.name
        )
    }

    /**
     * execute single process definitions api and get details of it.
     */
    suspend fun singleProcessDefinition(appDefinitionId: String) =
        ResponseListProcessDefinition.with(processesService.singleProcessDefinition(true, appDefinitionId))

    /**
     * execute process definitions apis and filter published process.
     */
    suspend fun processDefinitions() = ResponseListRuntimeProcessDefinition.with(processesService.processDefinitions())

    /**
     * execute the start-form apis to fetch the form presentation
     */
    suspend fun startForm(processDefinitionId: String) = ResponseListStartForm.with(
        processesService.startForm(processDefinitionId)
    )

    /**
     * Execute the start flow integration
     */
    suspend fun startWorkflow(processEntry: ProcessEntry?, items: String) = ProcessEntry.with(
        processesService.createProcessInstance(
            RequestProcessInstances(
                name = processEntry?.name,
                processDefinitionId = processEntry?.id,
                values = ValuesModel(
                    due = processEntry?.formattedDueDate,
                    message = processEntry?.description,
                    priority = if (processEntry?.priority != -1) PriorityModel(
                        id = getTaskPriority(processEntry?.priority ?: 0).name,
                        name = getTaskPriority(processEntry?.priority ?: 0).name
                    ) else null,
                    reviewer = getUser(processEntry?.startedBy),
                    reviewGroups = getGroup(processEntry?.startedBy),
                    items = items,
                    sendEmailNotifications = false
                )
            )
        )
    )

    private fun getUser(userGroupInfo: UserGroupDetails?): UserInfo? {
        return if (userGroupInfo?.isGroup == true)
            null
        else
            UserInfo(
                id = userGroupInfo?.id,
                firstName = userGroupInfo?.firstName,
                lastName = userGroupInfo?.lastName,
                email = userGroupInfo?.email
            )
    }

    private fun getGroup(userGroupInfo: UserGroupDetails?): GroupInfo? {
        return if (userGroupInfo?.isGroup == false)
            null
        else
            GroupInfo(
                id = userGroupInfo?.id,
                name = userGroupInfo?.name,
                externalId = userGroupInfo?.externalId,
                status = userGroupInfo?.status,
                parentGroupId = userGroupInfo?.parentGroupId,
                groups = userGroupInfo?.groups
            )
    }

    /**
     * saving the accountInfo data in preferences
     */
    fun saveSourceName(accountInfo: AccountInfoData) {
        val editor = sharedPrefs.edit()
        editor.putString(KEY_SOURCE_NAME, accountInfo.sourceName)
        editor.apply()
    }

    /**
     * Call to fetch account info from APS
     */
    suspend fun getAccountInfo() = ResponseAccountInfo.with(processesService.accountInfo())

    companion object {
        const val KEY_PROCESS_USER_ID = "process_user_id"
        const val KEY_PROCESS_USER_FULL_NAME = "process_user_full_name"
        const val KEY_PROCESS_USER_FIRST_NAME = "process_user_first_name"
        const val KEY_PROCESS_USER_LAST_NAME = "process_user_last_name"
        const val KEY_PROCESS_USER_EMAIL = "process_user_email"
        const val KEY_SOURCE_NAME = "source_name"
    }
}
