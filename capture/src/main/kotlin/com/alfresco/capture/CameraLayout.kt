package com.alfresco.capture

import android.content.Context
import android.util.AttributeSet
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.camera.view.PreviewView
import androidx.core.view.isVisible
import kotlin.math.min

class CameraLayout(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var topBar: ViewGroup
    private lateinit var previewHolder: ViewGroup
    private lateinit var shutterBar: ViewGroup
    private lateinit var modeBar: ViewGroup
    lateinit var viewFinder: PreviewView
    lateinit var focusView: View
    lateinit var shutterButton: ImageButton
    lateinit var cameraSwitchButton: ImageButton
    lateinit var closeButton: ImageButton
    lateinit var flashButton: ImageButton
    lateinit var messageView: TextView

    private val orientationAwareControls get() =
        listOf(
            cameraSwitchButton,
            flashButton,
            closeButton,
            messageView
        )

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
        previewHolder = findViewById(R.id.preview_holder)
        shutterBar = findViewById(R.id.shutter_bar)
        modeBar = findViewById(R.id.bottom_bar)

        viewFinder = findViewById(R.id.view_finder)
        focusView = findViewById(R.id.focus_view)
        shutterButton = findViewById(R.id.shutter_button)
        cameraSwitchButton = findViewById(R.id.camera_switch_button)
        closeButton = findViewById(R.id.close_button)
        flashButton = findViewById(R.id.flash_button)
        messageView = findViewById(R.id.message_view)

        initControls()
    }

    private fun initControls() {
        focusView.alpha = 0f
        cameraSwitchButton.isVisible = false
        flashButton.isVisible = false
        messageView.isVisible = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)

        val childMeasureHeightSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.AT_MOST)
        measureChildren(widthMeasureSpec, childMeasureHeightSpec)

        val finderHeight = min(parentHeight, (parentWidth * aspectRatio).toInt())
        previewHolder.measure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(finderHeight, MeasureSpec.EXACTLY)
        )

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = topBar.measuredHeight +
            previewHolder.measuredHeight +
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
        val finderHeight = previewHolder.measuredHeight
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
        previewHolder.layout(0, finderGuide, width, finderGuide + finderHeight)
        shutterBar.layout(0, shutterGuide, width, shutterGuide + shutterHeight)
        modeBar.layout(0, modeGuide, width, modeGuide + modeHeight)
    }

    private val orientationEventListener = object : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            // Doesn't support upside down orientation
            if (orientation == 180) return

            val rotation = when (orientation) {
                90 -> -90
                270 -> 90
                else -> 0
            }.toFloat()

            if (orientation != 180) {
                orientationAwareControls.map {
                    it.animate().rotation(rotation).start()
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        orientationEventListener.enable()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        orientationEventListener.disable()
    }
}
