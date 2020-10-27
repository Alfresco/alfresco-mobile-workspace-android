
package com.alfresco.content.actions

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.databinding.ActionBarFragmentBinding
import kotlin.math.min
import kotlinx.coroutines.delay

class ActionBarFragment : BaseMvRxFragment() {
    private val viewModel: ActionListViewModel by fragmentViewModel()
    private lateinit var binding: ActionBarFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActionBarFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        withState(viewModel) { addButtons(binding.container, it.actions) }

        lifecycleScope.on<ActionDelete> {
            // delayed back to present the toast
            delay(1000)
            requireActivity().onBackPressed()
        }
    }

    private fun addButtons(container: LinearLayout, actions: List<Action>) {
        val count = min(actions.size, MAX_ITEMS)
        for (i in 0 until count) {
            if (i == count - 1) {
                if (actions.size > MAX_ITEMS) {
                    container.addView(createMoreButton())
                } else {
                    container.addView(createButton(actions[i]))
                }
            } else {
                container.addView(createButton(actions[i]))
                container.addView(createSeparator())
            }
        }
    }

    private fun createButton(action: Action) =
        ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            minimumWidth = resources.getDimension(R.dimen.action_button_min_touch_target_size).toInt()
            minimumHeight = minimumWidth
            background = context.drawableFromAttribute(android.R.attr.actionBarItemBackground)
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
                1.0f
            )
        }

    private fun createMoreButton() =
        ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            minimumWidth = resources.getDimension(R.dimen.action_button_min_touch_target_size).toInt()
            minimumHeight = minimumWidth
            background = context.drawableFromAttribute(android.R.attr.actionBarItemBackground)
            setImageResource(R.drawable.ic_more_vert)
            setOnClickListener {
                // TODO: define more interaction
            }
        }

    private fun Context.drawableFromAttribute(attribute: Int): Drawable? {
        val attributes = obtainStyledAttributes(intArrayOf(attribute))
        val result = attributes.getDrawable(0)
        attributes.recycle()
        return result
    }

    override fun invalidate() = withState(viewModel) {
        binding.container.removeAllViews()
        addButtons(binding.container, it.actions)
    }

    private companion object {
        const val MAX_ITEMS = 3
    }
}
