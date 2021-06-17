package com.alfresco.capture

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.fetch.VideoFrameFileFetcher
import coil.load
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.capture.databinding.FragmentSaveBinding
import com.alfresco.ui.getDrawableForAttribute
import com.alfresco.ui.text

class SaveFragment : Fragment(), MavericksView {

    private val viewModel: CaptureViewModel by activityViewModel()
    private lateinit var binding: FragmentSaveBinding
    private val imageLoader: ImageLoader by lazy {
        val context = requireContext()
        ImageLoader.Builder(context)
            .componentRegistry {
                add(VideoFrameFileFetcher(context))
            }
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { goBack() }
            title = resources.getString(R.string.capture_nav_save_title)
        }

        if (savedInstanceState == null) {
            withState(viewModel) {
                binding.fileNameInputLayout.text = it.capture?.name ?: ""
            }
        }

        binding.fileNameInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                val valid = viewModel.isFilenameValid(s.toString())
                val empty = s.toString().isEmpty()

                binding.fileNameInputLayout.error = when {
                        !valid -> resources.getString(R.string.capture_file_name_invalid_chars)
                        empty -> resources.getString(R.string.capture_file_name_empty)
                        else -> null
                    }
                binding.saveButton.isEnabled = valid && !empty
            }
        })

        binding.saveButton.setOnClickListener {
            viewModel.save(
                binding.fileNameInput.text.toString(),
                binding.descriptionInput.text.toString()
            )
        }

        binding.preview.setOnClickListener { showPreview() }

        binding.deletePhotoButton.setOnClickListener { goBack() }

        viewModel.onSaveComplete = {
            val activity = requireActivity()
            val intent = Intent().apply { putExtra(CapturePhotoResultContract.OUTPUT_KEY, it) }
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        }
    }

    private fun goBack() {
        viewModel.clearCaptures()
        requireActivity().onBackPressed()
    }

    override fun invalidate(): Unit = withState(viewModel) {
        binding.preview.load(it.capture?.uri, imageLoader)
    }

    private fun showPreview() = withState(viewModel) {
        findNavController().navigate(
            R.id.action_saveFragment_to_previewFragment,
            PreviewArgs.bundle(
                it.capture?.uri.toString(),
                it.capture?.mimeType ?: ""
            )
        )
    }
}
