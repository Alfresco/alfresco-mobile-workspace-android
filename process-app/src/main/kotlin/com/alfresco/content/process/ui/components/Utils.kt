import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alfresco.content.process.ui.theme.AlfrescoGray90060
import com.alfresco.content.process.ui.theme.White60
import com.alfresco.content.process.ui.theme.isNightMode

@Composable
fun trailingIconColor() = if (isNightMode()) White60 else AlfrescoGray90060

fun Modifier.inputField() =
    this
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 12.dp) // Add padding or other modifiers as needed
