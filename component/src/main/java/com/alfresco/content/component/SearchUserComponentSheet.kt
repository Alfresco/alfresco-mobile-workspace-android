package com.alfresco.content.component

import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CompoundButton
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.component.databinding.SheetComponentSearchUserBinding
import com.alfresco.content.hideSoftInput
import com.alfresco.content.simpleController
import com.alfresco.ui.BottomSheetDialogFragment
import com.alfresco.ui.getDrawableForAttribute
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.regex.Pattern

/**
 * Marked as SearchUserComponentSheet class
 */
class SearchUserComponentSheet : BottomSheetDialogFragment(), MavericksView {

    internal val viewModel: SearchUserComponentViewModel by fragmentViewModel()
    lateinit var binding: SheetComponentSearchUserBinding

    var onApply: SearchUserComponentApplyCallback? = null
    var onCancel: SearchUserComponentCancelCallback? = null

    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetComponentSearchUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding.toolbar.apply {
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { dismiss() }
            navigationContentDescription = context.getString(R.string.text_back_button)
        }
        binding.recyclerView.setController(epoxyController)
        binding.searchView.requestFocus()
        setListeners()
    }

    private fun setListeners() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (isResumed) {
                    setSearchQuery(newText ?: "")
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                hideSoftInput()
                return true
            }
        })

        binding.searchByName.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                viewModel.searchByName = isChecked
                binding.searchByEmail.isChecked = !isChecked
                changeTab(button, binding.searchByEmail)
            }
        }

        binding.searchByEmail.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                viewModel.searchByName = !isChecked
                binding.searchByName.isChecked = !isChecked
                changeTab(button, binding.searchByName)
            }
        }
    }

    private fun changeTab(selected: CompoundButton, notSelected: CompoundButton) {
        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.alfresco_gray_radio_text_color))
        notSelected.setTextColor(ContextCompat.getColor(requireContext(), R.color.alfresco_gray_radio_text_color_60))
        setSearchQuery(binding.searchView.query.toString())
    }

    private fun setSearchQuery(query: String) {
        val term = cleanupSearchQuery(query)
        if (!viewModel.searchByName) {
            if (isValidString(term))
                executeSearch(term)
        } else executeSearch(term)
    }

    private fun executeSearch(term: String) {
        scrollToTop()
        viewModel.setSearchQuery(term)
    }

    private fun isValidString(str: String): Boolean {
        val emailAddressPattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return emailAddressPattern.matcher(str).matches()
    }

    private fun cleanupSearchQuery(query: String): String {
        return query.replace("\\s+".toRegex(), " ").trim()
    }

    private fun scrollToTop() {
        if (isResumed) {
            binding.recyclerView.layoutManager?.scrollToPosition(0)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.viewTreeObserver?.addOnGlobalLayoutListener {
            val bottomSheet =
                (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                BottomSheetBehavior.from<View>(it).apply {
                    peekHeight = Resources.getSystem().displayMetrics.heightPixels
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val outMetrics = DisplayMetrics()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = requireActivity().display
            display?.getRealMetrics(outMetrics)
        } else {
            val display = requireActivity().windowManager.defaultDisplay
            display.getMetrics(outMetrics)
        }
        binding.componentParent.layoutParams.height = outMetrics.heightPixels
        binding.componentParent.requestLayout()
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.loading.isVisible = state.requestUser is Loading

        epoxyController.requestModelBuild()
    }

    private fun epoxyController() = simpleController(viewModel) { state ->
        state.listUser.forEach { item ->
            listViewUserRow {
                id(item.id)
                data(item)
                clickListener { model, _, _, _ ->
                    onApply?.invoke(model.data())
                    dismiss()
                }
            }
        }
    }
}
