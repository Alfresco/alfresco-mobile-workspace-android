package com.alfresco.content.browse.preview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alfresco.content.browse.databinding.ActivityLocalPreviewBinding

/**
 * Mark as Preview Activity
 */
class LocalPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocalPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocalPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        title = intent.extras?.getString("title")
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is LocalPreviewFragment) {
            fragment.arguments = intent.extras
        }
    }

    companion object {
        const val KEY_PATH = "path"
        const val KEY_MIME_TYPE = "mimeType"
        const val KEY_TITLE = "title"
    }
}
