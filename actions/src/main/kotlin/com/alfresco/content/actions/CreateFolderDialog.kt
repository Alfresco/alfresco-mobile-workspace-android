package com.alfresco.content.actions

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.alfresco.content.actions.databinding.DialogCreateLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal typealias CreateFolderSuccessCallback = (String, String) -> Unit
internal typealias CreateFolderCancelCallback = () -> Unit

class CreateFolderDialog : DialogFragment() {

    private val binding: DialogCreateLayoutBinding by lazy {
        DialogCreateLayoutBinding.inflate(LayoutInflater.from(requireContext()), null, false)
    }

    private val positiveButton by lazy {
        (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
    }

    var onSuccess: CreateFolderSuccessCallback? = null
    var onCancel: CreateFolderCancelCallback? = null
    var isUpdate: Boolean = false
    var name: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(if (isUpdate) R.string.action_rename_file_folder else R.string.action_create_folder))
            .setNegativeButton(getString(R.string.action_folder_cancel)) { _, _ ->
                onCancel?.invoke()
                dialog?.dismiss()
            }
            .setPositiveButton(
                if (isUpdate) getString(R.string.action_text_save)
                else getString(R.string.action_folder_create)
            ) { _, _ ->
                onSuccess?.invoke(
                    binding.nameInput.text.toString(),
                    binding.descriptionInput.text.toString()
                )
            }
            .setView(binding.root)
            .show()

    override fun onStart() {
        super.onStart()

        binding.nameInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                validateInput(s.toString())
            }
        })
        if (isUpdate && !name.isNullOrEmpty()) {
            binding.nameInput.setText(name)
            positiveButton.isEnabled = true
        } else {
            // Default disabled
            positiveButton.isEnabled = false
        }
    }

    private fun validateInput(title: String) {
        val isValid = isFolderNameValid(title)
        val isEmpty = title.isEmpty()

        binding.nameInputLayout.error = when {
            !isValid -> resources.getString(R.string.action_folder_name_invalid_chars)
            isEmpty -> resources.getString(R.string.action_folder_name_empty)
            else -> null
        }

        positiveButton.isEnabled = isValid && !isEmpty
    }

    private companion object {
        fun isFolderNameValid(filename: String): Boolean {
            val reservedChars = "?:\"*|/\\<>\u0000"
            return filename.all { c -> reservedChars.indexOf(c) == -1 }
        }
    }

    data class Builder(
        val context: Context,
        val updateValue: Boolean,
        val name: String? = null,
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
                isUpdate = this@Builder.updateValue
                name = this@Builder.name
            }.show(fragmentManager, CreateFolderDialog::class.java.simpleName)
        }
    }
}
