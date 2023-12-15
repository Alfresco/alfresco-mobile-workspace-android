package com.alfresco.content.process.ui

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.mvrx.compose.mavericksActivityViewModel
import com.alfresco.content.process.FormViewModel
import com.alfresco.content.process.ui.components.TextInputField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormFragment(navController: NavController, extras: Bundle?) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Form Details")
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle navigation icon click */ }) {
                        // Add navigation icon here
                    }
                },
                actions = {
                    // Add actions if any
                },
            )
        },
        content = { padding ->
            FormDetailScreen(padding)
        },
    )
}

@Composable
fun FormDetailScreen(padding: PaddingValues) {
    // This will get or create a ViewModel scoped to the Activity.
    val viewModel: FormViewModel = mavericksActivityViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextInputField(1)
        // Add more Composables here based on your UI requirements
    }
}

@Preview
@Composable
fun PreviewProcessDetailScreen() {
    FormDetailScreen(PaddingValues(16.dp))
}
