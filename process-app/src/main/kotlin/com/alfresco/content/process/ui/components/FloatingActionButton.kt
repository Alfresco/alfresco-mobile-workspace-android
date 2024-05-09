package com.alfresco.content.process.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.compose.collectAsState
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.fragments.FormViewModel
import com.alfresco.content.process.ui.fragments.ProcessFragment
import com.alfresco.content.process.ui.utils.getContentList

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

                        val contentList = getContentList(state)

                        if (contentList.isNotEmpty()) {
                            viewModel.optionsModel = OptionsModel(id = query, name = name)
                            fragment.confirmContentQueuePrompt()
                        } else {
                            viewModel.performOutcomes(
                                OptionsModel(
                                    id = query.ifEmpty { name },
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
        containerColor = if (state.enabledOutcomes) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        icon = { Icon(Icons.Filled.PlaylistAdd, stringResource(id = R.string.accessibility_process_actions), tint = Color.White) },
        text = { Text(text = stringResource(id = R.string.title_actions), color = Color.White) },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
        ),
    )
}
