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
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentType
import com.alfresco.content.setSafeOnClickListener
import com.google.android.material.snackbar.Snackbar

class AdvancedSettingsFragment : DialogFragment() {
    private val viewModel: LoginViewModel by activityViewModels()
    private val rootView: View get() = requireView()
    private lateinit var binding: FragmentAuthSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_auth_settings, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.startEditing()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tieAuthType.inputType = 0
        binding.tieAuthType.setOnFocusChangeListener { textView, hasFocus ->
            if (hasFocus) textView.performClick()
        }
        binding.tieAuthType.setSafeOnClickListener {
            openAuthSelection()
        }
    }

    override fun onStart() {
        super.onStart()

        activity?.title = resources.getString(R.string.auth_settings_title)
        viewModel.setHasNavigation(true)

        observe(viewModel.onSaveSettings, ::onSave)
    }

    private fun onSave(
        @Suppress("UNUSED_PARAMETER") value: Int,
    ) {
        Snackbar.make(
            rootView,
            R.string.auth_settings_prompt_success,
            Snackbar.LENGTH_LONG,
        ).show()
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
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

    private fun openAuthSelection() {
        val authName = viewModel.authConfigEditor.authTypeName.value ?: ""
        val authValue = viewModel.authConfigEditor.authTypeValue.value ?: ""

        val componentData = ComponentData.with(
            outcomes = viewModel.authConfigEditor.listAuthType,
            name = authName,
            query = authValue,
            title = binding.tilAuthType.hint.toString(),
            selector = ComponentType.DROPDOWN_RADIO.value,
        )
        ComponentBuilder(requireContext(), componentData)
            .onApply { name, query, _ ->
                viewModel.authConfigEditor.onAuthChange(name, query)
            }
            .onReset { name, query, _ ->
                viewModel.authConfigEditor.onAuthChange(name, query)
            }
            .onCancel {
                viewModel.authConfigEditor.onAuthChange(authName, authValue)
            }
            .show()
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
