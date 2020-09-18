package com.alfresco.content.viewer.text

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alfresco.content.viewer.common.ContentDownloader
import java.io.File
import kotlinx.coroutines.launch

class TextViewerFragment(
    private val uri: String
) : Fragment(R.layout.viewer_text) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById<TextView>(R.id.textView)
        textView.movementMethod = ScrollingMovementMethod()
        val output = File(requireContext().cacheDir, "content.tmp")

        lifecycleScope.launch {
            ContentDownloader.downloadFileTo(uri, output.path)

            // progressIndicator.visibility = View.GONE
            textView.text = readFile(output.path)
        }
    }

    private fun readFile(fileName: String) =
        File(fileName).inputStream().readBytes().toString(Charsets.UTF_8)
}
