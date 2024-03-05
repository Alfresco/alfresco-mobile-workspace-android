package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.process.R

@Composable
fun FloatingActionButton(outcomes: List<OptionsModel>) {
    val context = LocalContext.current

    ExtendedFloatingActionButton(
        onClick = {
            val componentData = ComponentData.with(
                outcomes,
                "",
                "",
            )
            ComponentBuilder(context, componentData)
                .onApply { name, query, _ ->
                }
                .onReset { name, query, _ ->
                }
                .onCancel {
                }
                .show()
        },
        containerColor = MaterialTheme.colorScheme.primary,
        icon = { Icon(Icons.Filled.PlaylistAdd, stringResource(id = R.string.accessibility_process_actions)) },
        text = { Text(text = stringResource(id = R.string.title_actions)) },
        modifier = Modifier.navigationBarsPadding(),
    )
}
