package com.alfresco.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.alfresco.android.aims.R
import com.alfresco.android.aims.databinding.ContainerAuthBasicBinding
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.common.FragmentBuilder

class BasicAuthFragment : DialogFragment() {
    private val viewModel: LoginViewModel by activityViewModels()

    private var withCloud: Boolean = false

    private val rootView: View get() = requireView()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = DataBindingUtil.inflate<ContainerAuthBasicBinding>(inflater, R.layout.container_auth_basic, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Reset action bar title
        activity?.title = ""
        viewModel.setHasNavigation(true)

        updateUi()
    }

    private fun updateUi() {
        if (withCloud) {
            rootView.findViewById<View>(R.id.tvSigninTo).visibility = View.GONE
            rootView.findViewById<View>(R.id.tvConnectUrl).visibility = View.GONE

            rootView.findViewById<View>(R.id.tvBasicAuthInfoCloud).visibility = View.VISIBLE
        } else {
            rootView.findViewById<View>(R.id.tvSigninTo).visibility = View.VISIBLE
            rootView.findViewById<View>(R.id.tvConnectUrl).visibility = View.VISIBLE

            rootView.findViewById<View>(R.id.tvBasicAuthInfoCloud).visibility = View.GONE
        }
    }

    class Builder(parent: FragmentActivity) : FragmentBuilder(parent) {
        override val fragmentTag = TAG

        override fun build(args: Bundle): Fragment {
            val fragment = BasicAuthFragment()
            fragment.arguments = args
            return fragment
        }
    }

    companion object {
        val TAG: String = BasicAuthFragment::class.java.name

        fun with(activity: FragmentActivity): Builder = Builder(activity)
    }
}
