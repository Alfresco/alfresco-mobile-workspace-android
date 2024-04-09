package com.alfresco.content.process.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.airbnb.mvrx.compose.collectAsState
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.fragments.FormViewModel
import com.alfresco.content.process.ui.fragments.ProcessFragment

@Composable
fun FloatingActionButton(outcomes: List<OptionsModel>, fragment: ProcessFragment, viewModel: FormViewModel) {
    val context = LocalContext.current
    val state by viewModel.collectAsState()

    ExtendedFloatingActionButton(
        onClick = {
            if (state.enabledOutcomes) {
                val componentData = ComponentData.with(
                    outcomes,
                    "",
                    "",
                )
                ComponentBuilder(context, componentData)
                    .onApply { name, query, _ ->
                        val list = state.formFields.filter { it.type == FieldType.UPLOAD.value() }
                            .map { it.value as? List<*> ?: emptyList<Entry>() }.flatten()

                        val uploadList = state.formFields.filter { it.type == FieldType.UPLOAD.value() }

                        val entry = uploadList.flatMap { fieldsData ->
                            (fieldsData.value as? List<*>)?.mapNotNull { it as? Entry } ?: emptyList()
                        }.find { !it.isUpload }

                        if (entry != null) {
                            viewModel.optionsModel = OptionsModel(id = query, name = name)
                            fragment.confirmContentQueuePrompt()
                        } else {
                            viewModel.performOutcomes(
                                OptionsModel(
                                    id = query,
                                    name = name,
                                ),
                            )
                        }
                    }
                    .onReset { name, query, _ ->
                    }
                    .onCancel {
                    }
                    .show()
            }
        },
        containerColor = MaterialTheme.colorScheme.primary,
        icon = { Icon(Icons.Filled.PlaylistAdd, stringResource(id = R.string.accessibility_process_actions)) },
        text = { Text(text = stringResource(id = R.string.title_actions)) },
    )
}
