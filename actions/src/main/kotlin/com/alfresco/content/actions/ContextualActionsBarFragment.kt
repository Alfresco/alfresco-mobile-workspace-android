package com.alfresco.content.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.sheet.ProcessDefinitionsSheet
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.CommonRepository
import com.alfresco.content.data.CommonRepository.Companion.KEY_FEATURES_MOBILE
import com.alfresco.content.data.ContextualActionData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.MenuActions
import com.alfresco.content.data.MobileConfigDataEntry
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.getJsonFromSharedPrefs
import com.alfresco.events.on
import com.alfresco.ui.getDrawableForAttribute
import kotlinx.coroutines.delay

class ContextualActionsBarFragment : Fragment(), MavericksView, EntryListener {
    private val viewModel: ContextualActionsViewModel by fragmentViewModel()
    private lateinit var view: LinearLayout
    private var mobileConfigData: MobileConfigDataEntry? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        view =
            LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }
        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.on<ActionDelete> {
            // delayed back to present the toast
            delay(1000)
            requireActivity().onBackPressed()
        }
        viewModel.setEntryListener(this)
    }

    override fun invalidate() =
        withState(viewModel) {
            val entry = it.entries.first()

            (requireActivity() as AppCompatActivity).supportActionBar?.title = entry.name

            view.removeAllViews()
            addButtons(view, it.topActions, entry)
        }

    private fun addButtons(
        container: LinearLayout,
        actions: List<Action>,
        entry: Entry,
    ) {
        container.addView(createSeparator())

        for (action in actions) {
            if (action !is ActionDownload || entry.canCreateUpdate) {
                val downloadAction = createButton(action)

                mobileConfigData = getJsonFromSharedPrefs<MobileConfigDataEntry>(requireContext(), KEY_FEATURES_MOBILE)
                val menus = mobileConfigData?.featuresMobile?.menus ?: emptyList()

                if (CommonRepository().isActionEnabled(MenuActions.Download, menus)) {
                    container.addView(downloadAction)
                    container.addView(createSeparator())
                }
            }
        }

        if (actions.isNotEmpty()) {
            container.addView(createMoreButton())
            container.addView(createSeparator())
        }
    }

    private fun createButton(action: Action) =
        ImageButton(context).apply {
            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            minimumWidth = resources.getDimension(R.dimen.action_button_min_touch_target_size).toInt()
            minimumHeight = minimumWidth
            when (action) {
                is ActionDownload -> {
                    contentDescription = getString(R.string.accessibility_text_download)
                }

                is ActionAddFavorite -> {
                    contentDescription = getString(R.string.accessibility_text_add_favorite)
                }

                is ActionRemoveFavorite -> {
                    contentDescription = getString(R.string.accessibility_text_remove_favorite)
                }
            }
            background = context.getDrawableForAttribute(android.R.attr.actionBarItemBackground)
            setImageResource(action.icon)
            setOnClickListener {
                viewModel.execute(action)
            }
        }

    private fun createSeparator() =
        View(context).apply {
            layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f,
                )
        }

    private fun createMoreButton() =
        ImageButton(context).apply {
            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            minimumWidth = resources.getDimension(R.dimen.action_button_min_touch_target_size).toInt()
            minimumHeight = minimumWidth
            contentDescription = getString(R.string.accessibility_text_more)
            background = context.getDrawableForAttribute(android.R.attr.actionBarItemBackground)
            setImageResource(R.drawable.ic_more_vert)
            setOnClickListener {
                withState(viewModel) { state ->
                    ContextualActionsSheet.with(
                        ContextualActionData.withEntries(
                            state.entries,
                            mobileConfigData = mobileConfigData,
                        ),
                    ).show(childFragmentManager, null)
                }
            }
        }

    override fun onProcessStart(entries: List<ParentEntry>) {
        super.onProcessStart(entries)
        if (isAdded && isVisible && isResumed) {
            ProcessDefinitionsSheet.with(entries.map { it as Entry }).showNow(requireActivity().supportFragmentManager, null)
            requireActivity().supportFragmentManager.executePendingTransactions()
        }
    }
}
