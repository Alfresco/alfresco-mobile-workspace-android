package com.alfresco.pdf

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
import com.alfresco.scan.R
import com.alfresco.scan.databinding.DialogCreatePdfBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal typealias CreatePdfSuccessCallback = (String, String) -> Unit
internal typealias CreatePdfCancelCallback = () -> Unit

/**
 * Marked as CreatePdfDialog class
 */
class CreatePdfDialog : DialogFragment() {

    private val binding: DialogCreatePdfBinding by lazy {
        DialogCreatePdfBinding.inflate(LayoutInflater.from(requireContext()), null, false)
    }
    private val positiveButton by lazy {
        (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
    }
    var name: String = ""
    var onSuccess: CreatePdfSuccessCallback? = null
    var onCancel: CreatePdfCancelCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                getString(R.string.pdf_title_create)
            )
            .setNegativeButton(getString(R.string.pdf_confirmation_negative)) { _, _ ->
                onCancel?.invoke()
                dialog?.dismiss()
            }
            .setPositiveButton(
                getString(R.string.pdf_confirmation_positive)
            ) { _, _ ->
                onSuccess?.invoke(
                    binding.pdfNameInput.text.toString(),
                    binding.pdfDescriptionInput.text.toString()
                )
            }
            .setView(binding.root)
            .show()

    override fun onStart() {
        super.onStart()

        binding.pdfNameInputLayout.editText?.addTextChangedListener(object : TextWatcher {
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
        if (!name.isNullOrEmpty()) {
            binding.pdfNameInput.setText(name)
            positiveButton.isEnabled = true
        } else {
            // Default disabled
            positiveButton.isEnabled = false
        }
    }

    private fun validateInput(title: String) {
        val isValid = isFolderNameValid(title)
        val isEmpty = title.isEmpty()

        binding.pdfNameInputLayout.error = when {
            !isValid -> resources.getString(R.string.pdf_name_invalid_chars)
            isEmpty -> resources.getString(R.string.pdf_name_empty)
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

    /**
     * Builder to build the dialog
     */
    data class Builder(
        val context: Context,
        val name: String,
        var onSuccess: CreatePdfSuccessCallback? = null,
        var onCancel: CreatePdfCancelCallback? = null
    ) {

        /**
         * callback on success
         */
        fun onSuccess(callback: CreatePdfSuccessCallback?) =
            apply { this.onSuccess = callback }

        /**
         * callback on cancel
         */
        fun onCancel(callback: CreatePdfCancelCallback?) =
            apply { this.onCancel = callback }

        /**
         * display the pdf dialog
         */
        fun show() {
            val fragmentManager = when (context) {
                is AppCompatActivity -> context.supportFragmentManager
                is Fragment -> context.childFragmentManager
                else -> throw IllegalArgumentException()
            }
            CreatePdfDialog().apply {
                onSuccess = this@Builder.onSuccess
                onCancel = this@Builder.onCancel
                name = this@Builder.name
            }.show(fragmentManager, CreatePdfDialog::class.java.simpleName)
        }
    }
}
