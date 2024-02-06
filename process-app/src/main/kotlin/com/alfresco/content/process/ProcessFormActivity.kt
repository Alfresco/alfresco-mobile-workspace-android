package com.alfresco.content.process

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentContainerView
import com.alfresco.content.process.ui.ProcessFormFragment
import com.alfresco.content.process.ui.theme.AlfrescoBaseTheme

class ProcessFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlfrescoBaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    composeApp()
                }
            }
        }
    }

    private fun composeApp() {
        val fragmentManager = supportFragmentManager
        val containerId = resources.getIdentifier("frame_container", "id", packageName)

        // Check if the fragment is already added

        // Create FragmentContainerView and add it to the activity
        val fragmentContainer = FragmentContainerView(this).apply {
            id = containerId
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        setContentView(fragmentContainer)

        fragmentManager
            .beginTransaction()
            .replace(
                fragmentContainer.id,
                ProcessFormFragment().apply {
                    // Set your data using intent extras
                    arguments = intent.extras
                },
                "firstFragment",
            )
            .commit()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AlfrescoBaseTheme {
        NavigationComponent()
    }
}
