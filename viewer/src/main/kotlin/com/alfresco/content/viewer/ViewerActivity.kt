package com.alfresco.content.viewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alfresco.content.actions.Action
import com.alfresco.content.viewer.databinding.ActivityViewerBinding

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.title = intent.extras?.getString("title")
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeActionContentDescription(getString(R.string.label_navigation_back))
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        Action.showActionToasts(
            lifecycleScope,
            binding.root,
            binding.actionListBar
        )
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        if (fragment is ViewerFragment) {
            fragment.arguments = intent.extras
        }
    }
}
