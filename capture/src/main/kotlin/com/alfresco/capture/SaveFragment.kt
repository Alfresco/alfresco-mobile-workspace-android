package com.alfresco.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airbnb.epoxy.VisibilityState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.capture.databinding.FragmentSaveBinding
import com.alfresco.content.carouselBuilder
import com.alfresco.content.navigateToPreview
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute
import com.alfresco.ui.text

class SaveFragment : Fragment(), MavericksView {

    private val viewModel: CaptureViewModel by activityViewModel()
    private lateinit var binding: FragmentSaveBinding
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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
                binding.fileNameInputLayout.text = it.listCapture.first().name
            }
        }

        setListeners()

        binding.recyclerView.setController(epoxyController)

        EpoxyVisibilityTracker().attach(binding.recyclerView)
        Carousel.setDefaultGlobalSnapHelperFactory(object : Carousel.SnapHelperFactory() {
            override fun buildSnapHelper(context: Context?): SnapHelper {
                return PagerSnapHelper()
            }
        })

        binding.saveButton.setOnClickListener {
            viewModel.save()
        }

        viewModel.onSaveComplete = {
            val activity = requireActivity()
            val list = ArrayList<CaptureItem>(it)
            val intent = Intent().apply { putParcelableArrayListExtra(CapturePhotoResultContract.OUTPUT_KEY, list) }
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        }
    }

    private fun setListeners() {
        binding.fileNameInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                validateName(s.toString())
                viewModel.updateName(s.toString())
            }
        })

        binding.descriptionInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateDescription(s.toString())
            }
        })
    }

    private fun epoxyController() = simpleController(viewModel) { state ->
        if (state.listCapture.isNotEmpty()) {
            val viewsOnScreen = if (state.listCapture.size > 1) 1.5f else 1f
            carouselBuilder {
                id("carousel")
                numViewsToShowOnScreen(viewsOnScreen)
                padding(Carousel.Padding.dp(20, 8, 20, 8, 8))
                hasFixedSize(true)

                for (item in state.listCapture) {
                    this.listViewPreview {
                        id(item.uri.toString())
                        data(item)
                        previewClickListener { model, _, _, _ -> showPreview(model.data()) }
                        deletePhotoClickListener { model, _, _, _ -> delete(model.data()) }
                        onVisibilityStateChanged { _, _, visibilityState ->
                            if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
                                viewModel.copyVisibleItem(item)
                                binding.fileNameInputLayout.text = item.name
                                binding.descriptionInputLayout.text = item.description
                            }
                        }
                    }
                }
            }
        } else {
            requireActivity().runOnUiThread {
                goBack()
            }
        }
    }

    private fun validateName(name: String) {
        val valid = viewModel.isFilenameValid(name)
        val empty = name.isEmpty()

        binding.fileNameInputLayout.error = when {
            !valid -> resources.getString(R.string.capture_file_name_invalid_chars)
            empty -> resources.getString(R.string.capture_file_name_empty)
            else -> null
        }
        setSaveButtonState(valid && !empty)
    }

    private fun setSaveButtonState(isVisibleFieldValid: Boolean) = withState(viewModel) {
        binding.saveButton.isEnabled = isVisibleFieldValid && viewModel.isAllFileNameValid(it.listCapture)
    }

    private fun goBack() {
        viewModel.clearCaptureList()
        viewModel.clearCaptures()
        requireActivity().onBackPressed()
    }

    private fun delete(captureItem: CaptureItem) {
        viewModel.clearSingleCaptures(captureItem)
    }

    override fun invalidate(): Unit = withState(viewModel) {
        epoxyController.requestModelBuild()
    }

    private fun showPreview(captureItem: CaptureItem?) = withState(viewModel) {
        requireNotNull(captureItem)
        findNavController().navigateToPreview(
            captureItem.mimeType,
            captureItem.uri.toString(),
            captureItem.name,
        )
    }
}
