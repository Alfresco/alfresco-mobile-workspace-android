package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.content.models.AppConfigModel
import com.alfresco.content.models.CategoriesItem
import com.alfresco.content.models.Component
import com.alfresco.content.models.Options
import com.alfresco.content.models.SearchItem
import com.alfresco.content.models.Settings
import kotlinx.parcelize.Parcelize

/**
 * Mark ResponseAdvanceSearchConfig
 */
data class ResponseAdvanceSearchConfig(
    val search: List<AdvanceSearchModel>?
) {
    companion object {
        /**
         * returns the ResponseAdvanceSearchConfig on the basis of AppConfigModel
         */
        fun with(appConfigModel: AppConfigModel): ResponseAdvanceSearchConfig {
            return ResponseAdvanceSearchConfig(
                appConfigModel.search?.map { AdvanceSearchModel.with(it) } ?: emptyList()
            )
        }
    }
}

/**
 * Mark Advance Search Model
 */
@Parcelize
data class AdvanceSearchModel(
    val default: Boolean?,
    val name: String?,
    val filterWithContains: Boolean?,
    val categories: List<ChipModel>?,
    val resetButton: Boolean?
) : Parcelable {
    companion object {
        /**
         * returns the AdvanceSearchModel on the basis of SearchItem
         */
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

/**
 * Mark Chip Model
 */
@Parcelize
data class ChipModel(
    val expanded: Boolean? = null,
    val component: ChipComponentModel? = null,
    val name: String? = null,
    val id: String? = null,
    val enabled: Boolean? = null
) : Parcelable {
    companion object {
        /**
         * returns the ChipModel on the basis of CategoriesItem
         */
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

/**
 * Mark Chip Component Model
 */
@Parcelize
data class ChipComponentModel(
    val settings: ComponentSettingModel?,
    val selector: String?
) : Parcelable {
    companion object {
        /**
         * returns the ChipComponentModel on the basis of Component
         */
        fun with(component: Component): ChipComponentModel {
            return ChipComponentModel(
                selector = component.selector,
                settings = component.settings?.let { ComponentSettingModel.with(it) }
            )
        }
    }
}

/**
 * Mark Component Setting Model
 */
@Parcelize
data class ComponentSettingModel(
    val field: String?,
    val pattern: String?,
    val placeholder: String?,
    val pageSize: Int?,
    val operator: String?,
    val options: List<SettingOptionsModel>?,
    val min: Int?,
    val max: Int?,
    val step: Int?,
    val thumbLabel: Boolean?,
    val format: String?,
    val dateFormat: String?,
    val maxDate: String?
) : Parcelable {
    companion object {
        /**
         * returns the ComponentSettingModel on the basis of Settings
         */
        fun with(settings: Settings): ComponentSettingModel {
            return ComponentSettingModel(
                field = settings.field,
                pattern = settings.pattern,
                placeholder = settings.placeholder,
                pageSize = settings.pageSize,
                operator = settings.operator,
                options = settings.options?.map { SettingOptionsModel.with(it) } ?: emptyList(),
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

/**
 * Mark SettingOptions Model
 */
@Parcelize
data class SettingOptionsModel(
    val name: String?,
    val value: String?,
    val default: Boolean?
) : Parcelable {
    companion object {
        /**
         * returns the SettingOptionsModel on the basis of Options
         */
        fun with(options: Options): SettingOptionsModel {
            return SettingOptionsModel(
                name = options.name,
                value = options.value,
                default = options.default
            )
        }
    }
}
