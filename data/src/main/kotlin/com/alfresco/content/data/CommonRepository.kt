package com.alfresco.content.data

import com.alfresco.content.apis.MobileConfigApi
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import java.net.URL

class CommonRepository(val session: Session = SessionManager.requireSession) {

    private val fileMenuSingleActions = listOf(
        MenuActions.OpenWith, MenuActions.Download, MenuActions.AddFavourite, MenuActions.RemoveFavourite, MenuActions.StartProcess,
        MenuActions.Rename, MenuActions.Move, MenuActions.AddOffline, MenuActions.RemoveOffline, MenuActions.Trash,
    )

    private val fileMenuMultiActions = listOf(
        MenuActions.AddFavourite,
        MenuActions.RemoveFavourite,
        MenuActions.StartProcess,
        MenuActions.Move,
        MenuActions.AddOffline,
        MenuActions.RemoveOffline,
        MenuActions.Trash,
    )

    private val folderMenuSingleActions = listOf(
        MenuActions.AddFavourite,
        MenuActions.RemoveFavourite,
        MenuActions.Rename,
        MenuActions.Move,
        MenuActions.AddOffline,
        MenuActions.RemoveOffline,
        MenuActions.Trash,
    )

    private val folderMenuMultiActions = listOf(
        MenuActions.AddFavourite,
        MenuActions.RemoveFavourite,
        MenuActions.Move,
        MenuActions.AddOffline,
        MenuActions.RemoveOffline,
        MenuActions.Trash,
    )

    private val trashMenuActions = listOf(
        MenuActions.PermanentlyDelete,
        MenuActions.Restore,
    )

    private val service: MobileConfigApi by lazy {
        session.createService(MobileConfigApi::class.java)
    }

    suspend fun getMobileConfigData() {
        val data = MobileConfigDataEntry.with(service.getMobileConfig("https://${URL(session.account.serverUrl).host}/app-config.json"))

        saveJsonToSharedPrefs(session.context, KEY_FEATURES_MOBILE, data)
    }

    fun isAllMultiActionsEnabled(serverList: List<AppMenu>?, entries: List<Entry> = emptyList()): Boolean {
        val list = mutableListOf<MenuActions>()

        if (entries.isEmpty() || serverList?.isEmpty() == true) {
            return true
        }

        val hasFilesOnly = entries.all { it.isFile }
        val hasTrashedFilesFolders = entries.any { it.isTrashed }
        val hasFoldersOnly = entries.all { it.isFile }

        if (hasTrashedFilesFolders) {
            val enabledActions = trashMenuActions.filter { menuAction ->
                serverList?.any { it.id.equals(menuAction.value(), ignoreCase = true) && it.enabled } == true
            }
            list.addAll(enabledActions)
            return list.isNotEmpty()
        } else if (hasFilesOnly) {
            val enabledActions = fileMenuMultiActions.filter { menuAction ->
                serverList?.any { it.id.equals(menuAction.value(), ignoreCase = true) && it.enabled } == true
            }
            list.addAll(enabledActions)
            return list.isNotEmpty()
        } else if (hasFoldersOnly) {
            val enabledActions = folderMenuMultiActions.filter { menuAction ->
                serverList?.any { it.id.equals(menuAction.value(), ignoreCase = true) && it.enabled } == true
            }
            list.addAll(enabledActions)
            return list.isNotEmpty()
        } else {
            val enabledActions = folderMenuMultiActions.filter { menuAction ->
                serverList?.any { it.id.equals(menuAction.value(), ignoreCase = true) && it.enabled } == true
            }
            list.addAll(enabledActions)
            return list.isNotEmpty()
        }
    }

    fun isAllSingleFileActionsEnabled(serverList: List<AppMenu>?, entry: Entry): Boolean {
        val list = mutableListOf<MenuActions>()

        if (serverList?.isEmpty() == true) {
            return true
        }

        if (entry.isTrashed) {
            val enabledActions = trashMenuActions.filter { menuAction ->
                serverList?.any { it.id.equals(menuAction.value(), ignoreCase = true) && it.enabled } == true
            }
            list.addAll(enabledActions)
            return list.isNotEmpty()
        } else if (entry.isFile) {
            val enabledActions = fileMenuSingleActions.filter { menuAction ->
                serverList?.any { it.id.equals(menuAction.value(), ignoreCase = true) && it.enabled } == true
            }
            list.addAll(enabledActions)
            return list.isNotEmpty()
        } else {
            val enabledActions = folderMenuSingleActions.filter { menuAction ->
                serverList?.any { it.id.equals(menuAction.value(), ignoreCase = true) && it.enabled } == true
            }
            list.addAll(enabledActions)
            return list.isNotEmpty()
        }
    }

    companion object {
        const val KEY_FEATURES_MOBILE = "features_mobile"
    }
}
