package com.alfresco.content.actions

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.content.actions.databinding.DialogCreateFolderBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreateFolderDataModel(val name: String, val description: String) : Parcelable

internal data class CreateFolderState(val dataModel: CreateFolderDataModel? = CreateFolderDataModel("", "")) : MavericksState

internal class CreateFolderViewModel(
    val context: Context,
    state: CreateFolderState
) : MavericksViewModel<CreateFolderState>(state) {

    fun isFolderNameValid(filename: String): Boolean {
        val reservedChars = "?:\"*|/\\<>\u0000"
        return filename.all { c -> reservedChars.indexOf(c) == -1 }
    }

    companion object : MavericksViewModelFactory<CreateFolderViewModel, CreateFolderState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: CreateFolderState
        ) = CreateFolderViewModel(viewModelContext.activity(), state)
    }
}

internal typealias CreateFolderSuccessCallback = (String, String) -> Unit
internal typealias CreateFolderCancelCallback = () -> Unit

class CreateFolderDialog : DialogFragment(), MavericksView {

    private val viewModel: CreateFolderViewModel by fragmentViewModel()
    private lateinit var binding: DialogCreateFolderBinding

    var onSuccess: CreateFolderSuccessCallback? = null
    var onCancel: CreateFolderCancelCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DialogCreateFolderBinding.inflate(inflater, container, false)

        dialog?.window?.apply {
            setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20))
            requestFeature(Window.FEATURE_NO_TITLE)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.folderNameInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                val valid = viewModel.isFolderNameValid(s.toString())

                val empty = s.toString().isEmpty()

                binding.folderNameInputLayout.error = when {
                    !valid -> resources.getString(R.string.action_folder_name_invalid_chars)
                    empty -> resources.getString(R.string.action_folder_name_empty)
                    else -> null
                }

                val isEnabled = valid && !empty

                binding.createButton.isEnabled = isEnabled
            }
        })

        binding.cancelButton.setOnClickListener {
            onCancel?.invoke()
            dialog?.dismiss()
        }

        binding.createButton.setOnClickListener {
            onSuccess?.invoke(
                binding.folderNameInput.text.toString(),
                binding.folderDescriptionInput.text.toString()
            )

            dialog?.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun invalidate() {
    }

    data class Builder(
        val context: Context,
        var onSuccess: CreateFolderSuccessCallback? = null,
        var onCancel: CreateFolderCancelCallback? = null
    ) {

        fun onSuccess(callback: CreateFolderSuccessCallback?) =
            apply { this.onSuccess = callback }

        fun onCancel(callback: CreateFolderCancelCallback?) =
            apply { this.onCancel = callback }

        fun show() {
            val fragmentManager = when (context) {
                is AppCompatActivity -> context.supportFragmentManager
                is Fragment -> context.childFragmentManager
                else -> throw IllegalArgumentException()
            }
            CreateFolderDialog().apply {
                onSuccess = this@Builder.onSuccess
                onCancel = this@Builder.onCancel
            }.show(fragmentManager, CreateFolderDialog::class.java.simpleName)
        }
    }
}
