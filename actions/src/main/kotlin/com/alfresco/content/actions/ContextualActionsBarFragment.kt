package com.alfresco.content.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.data.ContextualActionData
import com.alfresco.events.on
import com.alfresco.ui.getDrawableForAttribute
import kotlinx.coroutines.delay

class ContextualActionsBarFragment : Fragment(), MavericksView {
    private val viewModel: ContextualActionsViewModel by fragmentViewModel()
    private lateinit var view: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        view = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.on<ActionDelete> {
            // delayed back to present the toast
            delay(1000)
            requireActivity().onBackPressed()
        }
    }

    override fun invalidate() = withState(viewModel) {
        view.removeAllViews()
        addButtons(view, it.topActions)
    }

    private fun addButtons(container: LinearLayout, actions: List<Action>) {
        container.addView(createSeparator())
        for (action in actions) {
            container.addView(createButton(action))
            container.addView(createSeparator())
        }
        if (actions.isNotEmpty()) {
            container.addView(createMoreButton())
            container.addView(createSeparator())
        }
    }

    private fun createButton(action: Action) =
        ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
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
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f,
            )
        }

    private fun createMoreButton() =
        ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
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
                    ContextualActionsSheet.with(ContextualActionData.withEntries(state.entries)).show(childFragmentManager, null)
                }
            }
        }
}
