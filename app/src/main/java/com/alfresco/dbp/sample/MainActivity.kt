package com.alfresco.dbp.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.alfresco.dbp.sample.features.BaseFragment

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            BaseFragment.PKCE_REQUEST_CODE -> {
                data?.let { mainViewModel.handleResult(it) }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}
