package com.alfresco.capture

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.camera.view.PreviewView

class CameraLayout(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var topBar: ViewGroup
    private lateinit var shutterBar: ViewGroup
    private lateinit var modeBar: ViewGroup
    lateinit var viewFinder: PreviewView
    lateinit var shutterButton: ImageButton
    lateinit var cameraSwitchButton: ImageButton

    var aspectRatio: Float = 4 / 3f
        set(value) {
            field = value
            requestLayout()
        }

    constructor(context: Context) :
        this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        this(context, attrs, defStyleAttr, 0)

    override fun onFinishInflate() {
        super.onFinishInflate()

        topBar = findViewById(R.id.top_bar)
        viewFinder = findViewById(R.id.view_finder)
        shutterBar = findViewById(R.id.shutter_bar)
        modeBar = findViewById(R.id.bottom_bar)

        shutterButton = findViewById(R.id.shutter_button)
        cameraSwitchButton = findViewById(R.id.camera_switch_button)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val childMeasureHeightSpec = MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.AT_MOST)

        measureChildren(widthMeasureSpec, childMeasureHeightSpec)

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = topBar.measuredHeight +
            viewFinder.measuredHeight +
            shutterBar.measuredHeight +
            modeBar.measuredHeight

        setMeasuredDimension(
            resolveSize(width, widthMeasureSpec),
            resolveSize(height, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top

        val topHeight = topBar.measuredHeight
        val finderHeight = (width * aspectRatio).toInt()
        val shutterHeight = shutterBar.measuredHeight
        val modeHeight = modeBar.measuredHeight

        val topGuide: Int
        val finderGuide: Int
        val shutterGuide: Int
        val modeGuide: Int

        when {
            // All elements fit vertically
            topHeight + finderHeight + shutterHeight + modeHeight <= height -> {
                val offset = (height - (topHeight + finderHeight + shutterHeight + modeHeight)) / 2
                topGuide = offset
                finderGuide = topGuide + topHeight
                shutterGuide = finderGuide + finderHeight
                modeGuide = shutterGuide + shutterHeight
            }

            // All elements except the shutter fit vertically
            topHeight + finderHeight + modeHeight <= height -> {
                val offset = (height - (topHeight + finderHeight + modeHeight)) / 2
                topGuide = offset
                finderGuide = topGuide + topHeight
                shutterGuide = finderGuide + finderHeight - shutterHeight
                modeGuide = finderGuide + finderHeight
            }

            // Only the top bar and finder fit vertically
            topHeight + finderHeight <= height -> {
                val offset = (height - (topHeight + finderHeight)) / 2
                topGuide = offset
                finderGuide = topGuide + topHeight
                modeGuide = finderGuide + finderHeight - modeHeight
                shutterGuide = modeGuide - shutterHeight
            }

            // Overlay everything on top of the finder
            else -> {
                val offset = (height - finderHeight) / 2
                topGuide = offset
                finderGuide = offset
                modeGuide = finderGuide + finderHeight - modeHeight
                shutterGuide = modeGuide - shutterHeight
            }
        }

        topBar.layout(0, topGuide, width, topGuide + topHeight)
        viewFinder.layout(0, finderGuide, width, finderGuide + finderHeight)
        shutterBar.layout(0, shutterGuide, width, shutterGuide + shutterHeight)
        modeBar.layout(0, modeGuide, width, modeGuide + modeHeight)
    }
}
