
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
import com.alfresco.content.actions.databinding.FragmentActionBarBinding
import kotlinx.coroutines.delay

class ActionBarFragment : BaseMvRxFragment() {
    private val viewModel: ActionListViewModel by fragmentViewModel()
    private lateinit var binding: FragmentActionBarBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActionBarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.on<ActionDelete> {
            // delayed back to present the toast
            delay(1000)
            requireActivity().onBackPressed()
        }
    }

    private fun addButtons(container: LinearLayout, actions: List<Action>) {
        container.addView(createSeparator())
        for (action in actions) {
            container.addView(createButton(action))
            container.addView(createSeparator())
        }
        container.addView(createMoreButton())
        container.addView(createSeparator())
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
                withState(viewModel) {
                    ActionListSheet(it.entry).show(childFragmentManager, null)
                }
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
        addButtons(binding.container, it.topActions)
    }

    private companion object {
        const val MAX_ITEMS = 3
    }
}
