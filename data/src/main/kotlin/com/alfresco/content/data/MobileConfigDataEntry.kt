package com.alfresco.content.data

import com.alfresco.content.models.DynamicMenu
import com.alfresco.content.models.Features
import com.alfresco.content.models.MobileConfigData

data class MobileConfigDataEntry(
    val featuresMobile: MobileFeatures? = null,
) {

    companion object {
        fun with(configData: MobileConfigData): MobileConfigDataEntry {
            return MobileConfigDataEntry(
                featuresMobile = MobileFeatures.with(configData.featuresMobile),
            )
        }
    }
}

data class MobileFeatures(
    val menus: List<AppMenu> = emptyList(),
) {

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

data class AppMenu(
    val id: String,
    val name: String,
    val enabled: Boolean,
) {
    companion object {
        fun with(menuData: DynamicMenu): AppMenu {
            return AppMenu(
                id = menuData.id ?: "",
                name = menuData.name ?: "",
                enabled = menuData.enabled ?: false,
            )
        }
    }
}
