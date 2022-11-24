package com.alfresco.content.browse.preview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alfresco.content.actions.Action
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.ActivityLocalPreviewBinding
import com.alfresco.content.data.Entry

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
        supportActionBar?.title = if (intent.extras?.containsKey(KEY_ENTRY_OBJ) == true) {
            (intent.extras?.getParcelable(KEY_ENTRY_OBJ) as Entry?)?.name
        } else intent.extras?.getString("title")
        supportActionBar?.setHomeActionContentDescription(getString(R.string.label_navigation_back))
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        Action.showActionToasts(
            lifecycleScope,
            findViewById(android.R.id.content),
            binding.bottomView
        )
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is LocalPreviewFragment) {
            fragment.arguments = intent.extras
        }
    }

    companion object {
        const val KEY_ENTRY_OBJ = "entryObj"
    }
}
