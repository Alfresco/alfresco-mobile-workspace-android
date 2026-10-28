package com.alfresco.capture

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alfresco.capture.databinding.ActivityPreviewBinding

/**
 * Mark as Preview Activity
 */
class PreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.extras?.getString("title")
        supportActionBar?.setHomeActionContentDescription(getString(R.string.label_navigation_back))
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is PreviewFragment) {
            fragment.arguments = intent.extras
        }
    }
}
