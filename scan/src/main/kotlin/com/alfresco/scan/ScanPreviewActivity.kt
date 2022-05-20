package com.alfresco.scan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alfresco.scan.databinding.ActivityScanPreviewBinding

/**
 * Mark as Preview Activity
 */
class ScanPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        title = intent.extras?.getString("title")
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is ScanPreviewFragment) {
            fragment.arguments = intent.extras
        }
    }
}
