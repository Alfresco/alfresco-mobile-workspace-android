package com.alfresco.capture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.RoundedCornersTransformation
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.capture.databinding.FragmentSaveBinding
import com.alfresco.ui.getDrawableForAttribute
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SaveFragment : Fragment(), MavericksView {

    private val viewModel: CaptureViewModel by activityViewModel()
    private lateinit var binding: FragmentSaveBinding

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

        binding.doneButton.setOnClickListener {
            viewModel.save(binding.fileNameInputLayout.editText?.text.toString())
        }

        viewModel.onUploadComplete = {
            requireActivity().finish()
        }

        binding.toolbar.apply {
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            title = resources.getString(R.string.capture_nav_save_title)
        }
    }

    override fun invalidate(): Unit = withState(viewModel) { state ->

        val fileNameInput = binding.fileNameInputLayout.editText
        requireNotNull(fileNameInput)

        // TODO: empty on rotation?
        if (fileNameInput.text.isEmpty()) {
            val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            val time: Date = Calendar.getInstance().time
            fileNameInput.setText("IMG_${formatter.format(time)}")
        }

        val path = "file://" + state.file
        binding.preview.load(path) {
            // TODO: proper transformation
            transformations(RoundedCornersTransformation(8f))
        }
    }
}
