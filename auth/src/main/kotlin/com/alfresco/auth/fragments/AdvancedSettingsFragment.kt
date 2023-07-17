package com.alfresco.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.alfresco.android.aims.R
import com.alfresco.android.aims.databinding.FragmentAuthSettingsBinding
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.auth.ui.observe
import com.alfresco.common.FragmentBuilder
import com.google.android.material.snackbar.Snackbar

class AdvancedSettingsFragment : DialogFragment() {

    private val viewModel: LoginViewModel by activityViewModels()
    private val rootView: View get() = requireView()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = DataBindingUtil.inflate<FragmentAuthSettingsBinding>(inflater, R.layout.fragment_auth_settings, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.startEditing()
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        activity?.title = resources.getString(R.string.auth_settings_title)
        viewModel.setHasNavigation(true)

        observe(viewModel.onSaveSettings, ::onSave)
    }

    private fun onSave(@Suppress("UNUSED_PARAMETER") value: Int) {
        Snackbar.make(
            rootView,
            R.string.auth_settings_prompt_success,
            Snackbar.LENGTH_LONG,
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_auth_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reset -> {
                viewModel.authConfigEditor.resetToDefaultConfig()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class Builder(parent: FragmentActivity) : FragmentBuilder(parent) {
        override val fragmentTag = TAG

        override fun build(args: Bundle): Fragment {
            val fragment = AdvancedSettingsFragment()
            fragment.arguments = args

            return fragment
        }
    }

    companion object {
        val TAG: String = AdvancedSettingsFragment::class.java.name

        fun with(activity: FragmentActivity): Builder = Builder(activity)
    }
}
