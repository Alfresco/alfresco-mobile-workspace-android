import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.BackButton
import com.alfresco.content.process.ui.theme.SeparateColorGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeTopBar() {
    val context = LocalContext.current
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.action_start_workflow),
                )
            },
            navigationIcon = {
                BackButton(onClick = { (context as Activity).finish() })
            },
        )
        Divider(color = SeparateColorGray, thickness = 1.dp)
    }
}
