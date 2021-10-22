package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.content.models.AppConfigModel
import com.alfresco.content.models.CategoriesItem
import com.alfresco.content.models.Component
import com.alfresco.content.models.Options
import com.alfresco.content.models.SearchItem
import com.alfresco.content.models.Settings
import kotlinx.parcelize.Parcelize

data class ResponseAdvanceSearchConfig(
    val search: List<AdvanceSearchModel>?
) {
    companion object {
        fun with(appConfigModel: AppConfigModel): ResponseAdvanceSearchConfig {
            return ResponseAdvanceSearchConfig(
                appConfigModel.search?.map { AdvanceSearchModel.with(it) } ?: emptyList()
            )
        }
    }
}

@Parcelize
data class AdvanceSearchModel(
    val default: Boolean?,
    val name: String?,
    val filterWithContains: Boolean?,
    val categories: List<ChipModel>?,
    val resetButton: Boolean?
) : Parcelable {
    companion object {
        fun with(searchItem: SearchItem): AdvanceSearchModel {
            return AdvanceSearchModel(
                name = searchItem.name,
                resetButton = searchItem.resetButton,
                categories = searchItem.categories?.map { ChipModel.with(it) } ?: emptyList(),
                filterWithContains = searchItem.filterWithContains,
                default = searchItem.default
            )
        }
    }
}

@Parcelize
data class ChipModel(
    val expanded: Boolean? = null,
    val component: ChipComponentModel? = null,
    val name: String? = null,
    val id: String? = null,
    val enabled: Boolean? = null
) : Parcelable {
    companion object {
        fun with(categoriesItem: CategoriesItem): ChipModel {
            return ChipModel(
                id = categoriesItem.id,
                name = categoriesItem.name,
                enabled = categoriesItem.enabled,
                expanded = categoriesItem.enabled,
                component = categoriesItem.component?.let { ChipComponentModel.with(it) }
            )
        }
    }
}

@Parcelize
data class ChipComponentModel(
    val settings: ComponentSettingModel?,
    val selector: String?
) : Parcelable {
    companion object {
        fun with(component: Component): ChipComponentModel {
            return ChipComponentModel(
                selector = component.selector,
                settings = component.settings?.let { ComponentSettingModel.with(it) }
            )
        }
    }
}

@Parcelize
data class ComponentSettingModel(
    val field: String?,
    val pattern: String?,
    val placeholder: String?,
    val pageSize: Int?,
    val operator: String?,
    val options: List<SettingOptions>?,
    val min: Int?,
    val max: Int?,
    val step: Int?,
    val thumbLabel: Boolean?,
    val format: String?,
    val dateFormat: String?,
    val maxDate: String?
) : Parcelable {
    companion object {
        fun with(settings: Settings): ComponentSettingModel {
            return ComponentSettingModel(
                field = settings.field,
                pattern = settings.pattern,
                placeholder = settings.placeholder,
                pageSize = settings.pageSize,
                operator = settings.operator,
                options = settings.options?.map { SettingOptions.with(it) } ?: emptyList(),
                min = settings.min,
                max = settings.max,
                step = settings.step,
                thumbLabel = settings.thumbLabel,
                format = settings.format,
                dateFormat = settings.dateFormat,
                maxDate = settings.maxDate
            )
        }
    }
}

@Parcelize
data class SettingOptions(
    val name: String?,
    val value: String?,
    val default: Boolean?
) : Parcelable {
    companion object {
        fun with(options: Options): SettingOptions {
            return SettingOptions(
                name = options.name,
                value = options.value,
                default = options.default
            )
        }
    }
}
