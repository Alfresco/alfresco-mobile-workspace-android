package com.alfresco.content.process.ui.utils

import android.content.Context
import android.os.Bundle
import androidx.navigation.NavController
import com.airbnb.mvrx.Mavericks
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentType
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.data.payloads.UploadData
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.fragments.FormViewState

fun actionsReadOnlyField(
    isTapped: Boolean, field: FieldsData, navController: NavController,
    state: FormViewState, context: Context
) {
    when (field.params?.field?.type?.lowercase()) {
        FieldType.UPLOAD.value() -> {
            if (isTapped && field.value is List<*> && (field.value as List<*>).isNotEmpty()) {
                val bundle = Bundle().apply {
                    putParcelable(
                        Mavericks.KEY_ARG,
                        UploadData(
                            field = field,
                            process = state.parent,
                        ),
                    )
                }
                navController.navigate(
                    R.id.action_nav_process_form_to_nav_attach_files,
                    bundle,
                )
            }
        }

        FieldType.TEXT.value(), FieldType.MULTI_LINE_TEXT.value() -> {
            ComponentBuilder(
                context,
                ComponentData(
                    name = field.name,
                    query = "",
                    value = field.value as? String ?: "",
                    selector = ComponentType.VIEW_TEXT.value,
                ),
            )
                .onApply { name, query, _ ->
                }
                .onReset { name, query, _ ->
                }
                .onCancel {
                }
                .show()
        }
    }
}