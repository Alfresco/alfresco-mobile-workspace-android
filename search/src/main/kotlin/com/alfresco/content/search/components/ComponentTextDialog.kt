package com.alfresco.content.search.components

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
import com.alfresco.content.search.R
import com.alfresco.content.search.databinding.DialogTextComponentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal typealias ComponentApplyCallback = (String) -> Unit
internal typealias ComponentCancelCallback = () -> Unit

class ComponentTextDialog : DialogFragment() {

    private val binding: DialogTextComponentBinding by lazy {
        DialogTextComponentBinding.inflate(LayoutInflater.from(requireContext()), null, false)
    }

    private val positiveButton by lazy {
        (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
    }

    var onSuccess: ComponentApplyCallback? = null
    var onCancel: ComponentCancelCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(requireContext().getString(R.string.title_text_filter))
            .setNegativeButton("Cancel") { _, _ ->
                onCancel?.invoke()
                dialog?.dismiss()
            }
            .setPositiveButton("Apply") { _, _ ->
                onSuccess?.invoke(
                    binding.folderNameInput.text.toString()
                )
            }
            .setView(binding.root)
            .show()

    override fun onStart() {
        super.onStart()

        binding.folderNameInputLayout.editText?.addTextChangedListener(object : TextWatcher {
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
        val isValid = isFolderNameValid(title)
        val isEmpty = title.isEmpty()

        binding.folderNameInputLayout.error = when {
            !isValid -> "Not Valid"
            isEmpty -> "Empty"
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
        var onApply: ComponentApplyCallback? = null,
        var onCancel: ComponentCancelCallback? = null
    ) {

        fun onApply(callback: ComponentApplyCallback?) =
            apply { this.onApply = callback }

        fun onCancel(callback: ComponentCancelCallback?) =
            apply { this.onCancel = callback }

        fun show() {
            val fragmentManager = when (context) {
                is AppCompatActivity -> context.supportFragmentManager
                is Fragment -> context.childFragmentManager
                else -> throw IllegalArgumentException()
            }
            ComponentTextDialog().apply {
                onSuccess = this@Builder.onApply
                onCancel = this@Builder.onCancel
            }.show(fragmentManager, ComponentTextDialog::class.java.simpleName)
        }
    }
}
