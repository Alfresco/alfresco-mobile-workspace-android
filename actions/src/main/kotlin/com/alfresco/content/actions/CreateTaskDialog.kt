package com.alfresco.content.actions

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.alfresco.content.actions.databinding.DialogCreateLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal typealias CreateTaskSuccessCallback = (String, String) -> Unit
internal typealias CreateTaskCancelCallback = () -> Unit

/**
 * Mark as CreateTaskDialog class
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
    var isUpdate: Boolean = false
    var dataObj: CreateMetadata? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(getString(if (isUpdate) R.string.action_update_task_name_description else R.string.action_create_task))
            .setNegativeButton(getString(R.string.action_folder_cancel)) { _, _ ->
                onCancel?.invoke()
                dialog?.dismiss()
            }
            .setPositiveButton(
                if (isUpdate) {
                    getString(R.string.action_text_save)
                } else {
                    getString(R.string.action_folder_create)
                },
            ) { _, _ ->
                onSuccess?.invoke(
                    binding.nameInput.text.toString(),
                    binding.descriptionInput.text.toString(),
                )
            }
            .setView(binding.root)
            .show()

    override fun onStart() {
        super.onStart()
        binding.nameInput.filters = arrayOf<InputFilter>(LengthFilter(255))
        binding.descriptionInput.filters = arrayOf<InputFilter>(LengthFilter(500))
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

        if (isUpdate && dataObj?.name != null) {
            binding.nameInput.setText(dataObj?.name)
            positiveButton.isEnabled = true
        } else {
            // Default disabled
            positiveButton.isEnabled = binding.nameInput.text.toString().isNotEmpty()
        }
        binding.descriptionInput.setText(dataObj?.description)
        binding.nameInput.requestFocus()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    private fun validateInput(title: String) {
        val isEmpty = title.isEmpty()

        binding.nameInputLayout.error = when {
            isEmpty -> resources.getString(R.string.action_task_name_empty)
            else -> null
        }

        positiveButton.isEnabled = !isEmpty
    }

    /**
     * Builder to build the create task dialog
     */
    data class Builder(
        val context: Context,
        val isUpdate: Boolean,
        val dataObj: CreateMetadata? = null,
        var onSuccess: CreateTaskSuccessCallback? = null,
        var onCancel: CreateTaskCancelCallback? = null,
    ) {

        /**
         * success callback if create the task
         */
        fun onSuccess(callback: CreateTaskSuccessCallback?) =
            apply { this.onSuccess = callback }

        /**
         * cancel callback if dismiss the dialog
         */
        fun onCancel(callback: CreateTaskCancelCallback?) =
            apply { this.onCancel = callback }

        /**
         * It will show the create task dialog
         */
        fun show() {
            val fragmentManager = when (context) {
                is AppCompatActivity -> context.supportFragmentManager
                is Fragment -> context.childFragmentManager
                else -> throw IllegalArgumentException()
            }
            CreateTaskDialog().apply {
                onSuccess = this@Builder.onSuccess
                onCancel = this@Builder.onCancel
                isUpdate = this@Builder.isUpdate
                dataObj = this@Builder.dataObj
            }.show(fragmentManager, CreateTaskDialog::class.java.simpleName)
        }
    }
}
