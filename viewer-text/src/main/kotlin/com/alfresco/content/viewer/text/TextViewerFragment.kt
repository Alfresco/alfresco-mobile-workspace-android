package com.alfresco.content.viewer.text

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alfresco.content.data.BrowseRepository
import kotlinx.coroutines.launch

class TextViewerFragment(
    private val documentId: String,
    private val mimeType: String
) : Fragment(R.layout.viewer_text) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Stream data and improve performance
        val textView = view.findViewById<TextView>(R.id.textView)
        textView.movementMethod = ScrollingMovementMethod()
        lifecycleScope.launch {
            val content = BrowseRepository().fetchContent(documentId)
            textView.text = content
        }
    }
}
