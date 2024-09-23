package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.content.models.DynamicMenu
import com.alfresco.content.models.Features
import com.alfresco.content.models.MobileConfigData
import kotlinx.parcelize.Parcelize

@Parcelize
data class MobileConfigDataEntry(
    val featuresMobile: MobileFeatures? = null,
) : Parcelable {

    companion object {

        fun with(configData: MobileConfigData): MobileConfigDataEntry {
            return MobileConfigDataEntry(
                featuresMobile = MobileFeatures.with(configData.featuresMobile),
            )
        }
    }
}

@Parcelize
data class MobileFeatures(
    val menus: List<AppMenu> = emptyList(),
) : Parcelable {

    companion object {
        fun with(features: Features?): MobileFeatures {
            return MobileFeatures(
                menus = features?.dynamicMenus?.map {
                    AppMenu.with(
                        it,
                    )
                } ?: emptyList(),
            )
        }
    }
}

@Parcelize
data class AppMenu(
    val id: String,
    val enabled: Boolean,
) : Parcelable {
    companion object {
        fun with(menuData: DynamicMenu): AppMenu {
            return AppMenu(
                id = menuData.id ?: "",
                enabled = menuData.enabled ?: false,
            )
        }
    }
}

enum class MenuActions(val value: String) {
    AddFavourite("app.menu.addFavourite"),
    RemoveFavourite("app.menu.removeFavourite"),
    Rename("app.menu.rename"),
    Move("app.menu.move"),
    OpenWith("app.menu.openWith"),
    Download("app.menu.download"),
    Trash("app.menu.trash"),
    AddOffline("app.menu.addOffline"),
    RemoveOffline("app.menu.removeOffline"),
    Restore("app.menu.restore"),
    PermanentlyDelete("app.menu.permanentlyDelete"),
    StartProcess("app.menu.startProcess"),
    ;

    fun value() = value.lowercase()
}
