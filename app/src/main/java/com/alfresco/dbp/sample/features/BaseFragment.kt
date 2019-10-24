package com.alfresco.dbp.sample.features

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.alfresco.dbp.sample.MainViewModel
import com.alfresco.dbp.sample.PkceAuthUiModel
import com.alfresco.dbp.sample.R
import com.alfresco.dbp.sample.common.navigateToRefreshToken
import com.alfresco.dbp.sample.common.observe
import kotlinx.android.synthetic.main.fragment_base.*

class BaseFragment : Fragment(R.layout.fragment_base) {

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // sample alfresco url
        tilAlfrescoUrl.editText?.setText("http://alfresco-identity-service.mobile.dev.alfresco.me")

        btnAdvancedSettings.setOnClickListener {
            findNavController().navigate(BaseFragmentDirections.navigateToAdvancedSettings())
        }

        btnLogin.setOnClickListener {
            mainViewModel.initiateLogin(tilAlfrescoUrl.editText?.text.toString(), activity!!, PKCE_REQUEST_CODE)
        }

        observe(mainViewModel.authResult, ::onResult)
    }

    private fun onResult(authUiModel: PkceAuthUiModel) {
        if (authUiModel.success) {
            navigateToRefreshToken()
        } else {
            onError(authUiModel.error)
        }
    }

    private fun onError(errorMessage: String?) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val PKCE_REQUEST_CODE = 30
    }
}
