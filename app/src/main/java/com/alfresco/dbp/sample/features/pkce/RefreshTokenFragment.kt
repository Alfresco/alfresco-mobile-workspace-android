package com.alfresco.dbp.sample.features.pkce

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.alfresco.dbp.sample.MainActivity
import com.alfresco.dbp.sample.MainViewModel
import com.alfresco.dbp.sample.PkceRefreshUiModel
import com.alfresco.dbp.sample.R
import com.alfresco.dbp.sample.common.observe
import kotlinx.android.synthetic.main.fragment_refresh_token.*

class RefreshTokenFragment : Fragment(R.layout.fragment_refresh_token) {

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSignOut.setOnClickListener {
            mainViewModel.signOut()
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(intent)
        }

        btnRefreshToken.setOnClickListener {
            mainViewModel.refreshToken()
        }

        observe(mainViewModel.refreshResult, ::displayRefreshToken)
    }

    private fun displayRefreshToken(refreshUiModel: PkceRefreshUiModel) {
        if (refreshUiModel.success) {
            Toast.makeText(context, "Success", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, refreshUiModel.error, Toast.LENGTH_LONG).show()
        }
    }
}
