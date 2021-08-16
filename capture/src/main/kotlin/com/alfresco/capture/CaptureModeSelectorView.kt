package com.alfresco.capture

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper

class CaptureModeSelectorView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    var modes: List<CaptureMode> = listOf(CaptureMode.Photo, CaptureMode.Video)
        set(value) {
            field = value
            recyclerView.adapter = Adapter(modes.map { it.title(context) })
        }
    var onMode: ((CaptureMode) -> Unit)? = null
    private val recyclerView = createRecyclerView()

    init {
        addView(recyclerView)
        recyclerView.doOnPreDraw {
            setActive(0)
        }
    }

    constructor(context: Context) :
        this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        this(context, attrs, defStyleAttr, 0)

    private fun createRecyclerView() =
        RecyclerView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            adapter = Adapter(modes.map { it.title(context) })
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            addItemDecoration(
                SpacingDecoration(
                    resources.getDimension(R.dimen.capture_button_min_spacing).toInt()
                )
            )
            addItemDecoration(BoundsOffsetDecoration())
        }.also {
            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(it)
            it.addOnScrollListener(OnSnapScrollListener(snapHelper, onSnap = this::onItemSelected))
        }

    private fun onItemSelected(position: Int) {
        setActive(position)
        onMode?.invoke(modes[position])
    }

    fun retainLastState(position: Int) {
        setActive(position)
        recyclerView.layoutManager?.scrollToPosition(position)
        recyclerView.findViewHolderForAdapterPosition(position)?.itemView?.isEnabled = true
    }

    private fun setActive(position: Int) {
        recyclerView.children.forEach {
            it.isActivated = false
        }
        recyclerView.findViewHolderForAdapterPosition(position)?.itemView?.isActivated = true
    }

    private inner class Adapter(private val dataSet: List<String>) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        inner class ViewHolder(val view: ModeView) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) =
            ViewHolder(ModeView(viewGroup.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.WRAP_CONTENT,
                    resources.getDimension(R.dimen.capture_button_size).toInt()
                )
            })

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.view.apply {
                text = dataSet[position]
                setOnClickListener {
                    (viewHolder.view.parent as RecyclerView)
                        .smoothScrollToCenteredPosition(position)
                }
            }
        }

        override fun getItemCount() = dataSet.size
    }

    private class ModeView(
        context: Context
    ) : AppCompatTextView(
        ContextThemeWrapper(context, R.style.Widget_Alfresco_Camera_Mode_Button),
        null,
        0
    ) {
        init {
            gravity = Gravity.CENTER
            val pad = resources.getDimension(R.dimen.capture_button_padding).toInt()
            setPadding(pad, 0, pad, 0)
        }
    }

    private class SpacingDecoration(@Px private val innerSpacing: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val itemPosition = parent.getChildAdapterPosition(view)

            outRect.left = if (itemPosition == 0) 0 else innerSpacing / 2
            outRect.right = if (itemPosition == state.itemCount - 1) 0 else innerSpacing / 2
        }
    }

    /** Offset the first and last items to center them */
    private class BoundsOffsetDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val itemPosition = parent.getChildAdapterPosition(view)

            view.measure(
                MeasureSpec.makeMeasureSpec(parent.measuredWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(parent.measuredHeight, MeasureSpec.UNSPECIFIED)
            )
            val itemWidth = view.measuredWidth
            val offset = (parent.measuredWidth - itemWidth) / 2

            if (itemPosition == 0) {
                outRect.left = offset
            } else if (itemPosition == state.itemCount - 1) {
                outRect.right = offset
            }
        }
    }

    private inner class OnSnapScrollListener(
        private val snapHelper: SnapHelper,
        val onSnap: (Int) -> Unit
    ) : RecyclerView.OnScrollListener() {
        private var snapPosition = RecyclerView.NO_POSITION

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val snapPosition = snapHelper.getSnapPosition(recyclerView)
                val snapPositionChanged = this.snapPosition != snapPosition
                if (snapPositionChanged) {
                    onSnap(snapPosition)
                    this.snapPosition = snapPosition
                }
            }
        }
    }

    fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
        val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        return layoutManager.getPosition(snapView)
    }

    private fun RecyclerView.smoothScrollToCenteredPosition(position: Int) {
        val smoothScroller = object : LinearSmoothScroller(context) {
            private val MILLISECONDS_PER_INCH = 65f

            override fun calculateDxToMakeVisible(view: View?, snapPreference: Int): Int {
                val dxToStart = super.calculateDxToMakeVisible(view, SNAP_TO_START)
                val dxToEnd = super.calculateDxToMakeVisible(view, SNAP_TO_END)

                return (dxToStart + dxToEnd) / 2
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }
        }

        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }
}