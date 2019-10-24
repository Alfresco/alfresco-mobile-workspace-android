package com.alfresco.dbp.sample.features.pkce

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.alfresco.auth.pkce.PkceAuthConfig
import com.alfresco.dbp.sample.MainViewModel
import com.alfresco.dbp.sample.R
import com.alfresco.dbp.sample.common.observe
import kotlinx.android.synthetic.main.fragment_advanced_settings.*

class AdvancedSettingsFragment : Fragment(R.layout.fragment_advanced_settings) {

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSave.setOnClickListener {
            mainViewModel.changeSettings(
                    tilRealm.editText?.text.toString(),
                    tilClientId.editText?.text.toString(),
                    tilRedirectUrl.editText?.text.toString())
            findNavController().popBackStack()
        }

        mainViewModel.apply {
            observe(pkceAuthConfig, ::showDetails)
        }
    }

    private fun showDetails(pkceAuthConfig: PkceAuthConfig) {
        tilClientId.editText?.setText(pkceAuthConfig.clientId)
        tilRedirectUrl.editText?.setText(pkceAuthConfig.redirectUrl)
        tilRealm.editText?.setText(pkceAuthConfig.realm)
    }
}
