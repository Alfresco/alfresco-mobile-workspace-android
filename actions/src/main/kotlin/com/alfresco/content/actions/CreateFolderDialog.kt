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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.capture.R
import com.alfresco.content.actions.databinding.DialogCreateFolderBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreateFolderDataModel(val name: String, val description: String) : Parcelable

internal data class CreateFolderState(val dataModel: CreateFolderDataModel? = CreateFolderDataModel("", "")) : MavericksState

internal class CreateFolderViewModel(
    val context: Context,
    state: CreateFolderState
) : MavericksViewModel<CreateFolderState>(state) {

    var onCreateComplete: ((CreateFolderDataModel) -> Unit)? = null

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

    fun create(folderName: String, description: String) = withState {

        requireNotNull(it.dataModel)

        onCreateComplete?.invoke(
            it.dataModel.copy(
                name = folderName,
                description = description
            )
        )
    }
}

class CreateFolderDialog : DialogFragment(), MavericksView {

    private val viewModel: CreateFolderViewModel by fragmentViewModel()
    private lateinit var binding: DialogCreateFolderBinding


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
                    !valid -> resources.getString(R.string.capture_folder_name_invalid_chars)
                    empty -> resources.getString(R.string.capture_folder_name_empty)
                    else -> null
                }

                val isEnabled = valid && !empty

                println("CreateFolderDialog.afterTextChanged $isEnabled")

                binding.tvCreate.isEnabled = isEnabled
            }
        })

        binding.tvCancel.setOnClickListener {
            setResult(null)
            dialog?.dismiss()
        }

        binding.tvCreate.setOnClickListener {
            viewModel.create(
                binding.folderNameInput.text.toString(),
                binding.folderDescriptionInput.text.toString()
            )
        }

        viewModel.onCreateComplete = {
            setResult(it)
            dialog?.dismiss()
        }
    }

    private fun setResult(obj: CreateFolderDataModel?) {
        val result = Bundle().apply {
            putParcelable(CreateFolderFragment.DATA_OBJ, obj)
        }
        setFragmentResult(CreateFolderFragment.REQUEST_KEY, result)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun invalidate() {

    }


}