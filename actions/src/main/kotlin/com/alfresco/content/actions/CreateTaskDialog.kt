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

internal typealias CreateTaskSuccessCallback = (String, String) -> Unit
internal typealias CreateTaskCancelCallback = () -> Unit

/**
 * Marked as CreateTaskDialog
 */
class CreateTaskDialog : DialogFragment() {

    private val binding: DialogCreateLayoutBinding by lazy {
        DialogCreateLayoutBinding.inflate(LayoutInflater.from(requireContext()), null, false)
    }

    private val positiveButton by lazy {
        (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
    }

    var onSuccess: CreateTaskSuccessCallback? = null
    var onCancel: CreateTaskCancelCallback? = null
    var name: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.action_create_task))
            .setNegativeButton(getString(R.string.action_folder_cancel)) { _, _ ->
                onCancel?.invoke()
                dialog?.dismiss()
            }
            .setPositiveButton(
                getString(R.string.action_task_next)
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
        binding.nameInput.maxLines = 255
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
        // Default disabled
        positiveButton.isEnabled = false
    }

    private fun validateInput(title: String) {
        val isValid = isTaskNameValid(title)
        val isEmpty = title.isEmpty()

        binding.nameInputLayout.error = when {
            !isValid -> resources.getString(R.string.action_task_name_invalid_chars)
            isEmpty -> resources.getString(R.string.action_task_name_empty)
            else -> null
        }

        positiveButton.isEnabled = isValid && !isEmpty
    }

    private companion object {
        fun isTaskNameValid(filename: String): Boolean {
            val reservedChars = "?:\"*|/\\<>\u0000"
            return filename.all { c -> reservedChars.indexOf(c) == -1 }
        }
    }

    data class Builder(
        val context: Context,
        val name: String? = null,
        var onSuccess: CreateTaskSuccessCallback? = null,
        var onCancel: CreateTaskCancelCallback? = null
    ) {

        fun onSuccess(callback: CreateTaskSuccessCallback?) =
            apply { this.onSuccess = callback }

        fun onCancel(callback: CreateTaskCancelCallback?) =
            apply { this.onCancel = callback }

        fun show() {
            val fragmentManager = when (context) {
                is AppCompatActivity -> context.supportFragmentManager
                is Fragment -> context.childFragmentManager
                else -> throw IllegalArgumentException()
            }
            CreateTaskDialog().apply {
                onSuccess = this@Builder.onSuccess
                onCancel = this@Builder.onCancel
                name = this@Builder.name
            }.show(fragmentManager, CreateTaskDialog::class.java.simpleName)
        }
    }
}
