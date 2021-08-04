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
import com.airbnb.epoxy.carousel
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.capture.databinding.FragmentSaveBinding
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute
import com.alfresco.ui.text
import java.util.ArrayList

class SaveFragment : Fragment(), MavericksView {

    private val viewModel: CaptureViewModel by activityViewModel()
    private lateinit var binding: FragmentSaveBinding
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }

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
                binding.fileNameInputLayout.text = it.listCapture.first()?.name ?: ""
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
                val valid = viewModel.isFilenameValid(s.toString())
                val empty = s.toString().isEmpty()

                binding.fileNameInputLayout.error = when {
                    !valid -> resources.getString(R.string.capture_file_name_invalid_chars)
                    empty -> resources.getString(R.string.capture_file_name_empty)
                    else -> null
                }
                viewModel.updateName(s.toString())

                binding.saveButton.isEnabled = valid && !empty
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
            carousel {
                id("This is a ViewPager.")
                numViewsToShowOnScreen(viewsOnScreen)
                paddingRes(R.dimen.view_pager_item_padding)
                models(state.listCapture.mapIndexed { _, item ->
                    item?.let {
                        ListViewPreviewModel_()
                            .id(it.id)
                            .data(it)
                            .previewClickListener { model, _, _, _ -> showPreview(model.data()) }
                            .deletePhotoClickListener { model, _, _, _ -> delete(model.data()) }
                            .onVisibilityStateChanged { _, _, visibilityState ->
                                if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
                                    viewModel.copyVisibleItem(item)
                                    binding.fileNameInputLayout.text = item.name
                                }
                            }
                    }
                })
            }
        } else {
            requireActivity().runOnUiThread {
                goBack()
            }
        }
    }

    private fun goBack() {
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

        findNavController().navigate(
            R.id.action_saveFragment_to_previewFragment,
            PreviewArgs.bundle(
                captureItem.uri.toString(),
                captureItem.mimeType
            )
        )
    }
}
