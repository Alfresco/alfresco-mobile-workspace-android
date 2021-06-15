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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraLayout(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var topBar: ViewGroup
    private lateinit var previewHolder: ViewGroup
    private lateinit var onFrameControls: ViewGroup
    private lateinit var shutterBar: ViewGroup
    private lateinit var modeBar: ViewGroup
    lateinit var viewFinder: PreviewView
    lateinit var focusView: View
    lateinit var zoomTextView: TextView
    lateinit var shutterButton: ShutterButton
    lateinit var cameraSwitchButton: ImageButton
    lateinit var closeButton: ImageButton
    lateinit var flashButton: ImageButton
    lateinit var flashMenu: FlashMenu
    lateinit var messageView: TextView
    lateinit var modeSelectorView: CaptureModeSelectorView

    private val orientationAwareControls get() =
        listOf(
            cameraSwitchButton,
            flashButton,
            closeButton,
            zoomTextView,
            messageView,
            flashMenu
        )
    private var controlRotation = 0
    private var deviceOrientation = 0

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
        onFrameControls = findViewById(R.id.on_frame_controls)
        shutterBar = findViewById(R.id.shutter_bar)
        modeBar = findViewById(R.id.mode_bar)

        viewFinder = findViewById(R.id.view_finder)
        focusView = findViewById(R.id.focus_view)
        zoomTextView = findViewById(R.id.zoom_text)
        shutterButton = findViewById(R.id.shutter_button)
        cameraSwitchButton = findViewById(R.id.camera_switch_button)
        closeButton = findViewById(R.id.close_button)
        flashButton = findViewById(R.id.flash_button)
        flashMenu = findViewById(R.id.flash_menu)
        messageView = findViewById(R.id.message_view)
        modeSelectorView = findViewById(R.id.mode_selector)

        initControls()
    }

    private fun initControls() {
        focusView.alpha = 0f
        cameraSwitchButton.isVisible = false
        flashButton.isVisible = false
        flashMenu.isVisible = false
        messageView.isVisible = false
        modeBar.isVisible = BuildConfig.DEBUG
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

        // Re-measure flashMenu at 1:1 ratio
        val flashMenuSize = max(flashMenu.measuredHeight, flashMenu.measuredWidth)
        val flashMenuMeasureSpec = MeasureSpec.makeMeasureSpec(flashMenuSize, MeasureSpec.EXACTLY)
        flashMenu.measure(flashMenuMeasureSpec, flashMenuMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top

        val topHeight = topBar.measuredHeight
        val shutterHeight = shutterBar.measuredHeight
        var modeHeight = modeBar.measuredHeight

        val topGuide: Int
        val finderGuide: Int
        val shutterGuide: Int
        val modeGuide: Int

        val expandedFinder = min(height, (width * CaptureMode.RATIO_16_9).toInt())
        val compactFinder = min(height, (width * CaptureMode.RATIO_4_3).toInt())
        val currentFinder = min(height, (width * aspectRatio).toInt())
        val finderDiff = expandedFinder - compactFinder

        // Calculate layout in expanded viewfinder size, and then redistribute elements
        // to avoid moving them when switching between modes.
        when {
            // The top bar fits vertically outside the viewfinder
            topHeight + expandedFinder <= height -> {
                modeHeight += max(0, finderDiff - (shutterHeight + modeHeight))

                val offset = (height - (topHeight + expandedFinder)) / 2
                topGuide = offset
                finderGuide = topGuide + topHeight
                modeGuide = finderGuide + expandedFinder - modeHeight
                shutterGuide = modeGuide - shutterHeight
            }

            // The top bar overlays the viewfinder
            else -> {
                modeHeight += max(0, finderDiff - shutterHeight - modeHeight)

                val offset = (height - expandedFinder) / 2
                topGuide = offset
                finderGuide = offset
                modeGuide = finderGuide + expandedFinder - modeHeight
                shutterGuide = modeGuide - shutterHeight
            }
        }

        topBar.layout(0, topGuide, width, topGuide + topHeight)
        previewHolder.layout(0, finderGuide, width, finderGuide + currentFinder)
        shutterBar.layout(0, shutterGuide, width, shutterGuide + shutterHeight)
        onFrameControls.layout(0, topGuide + topHeight, width, shutterGuide)
        modeBar.layout(0, modeGuide, width, modeGuide + modeHeight)
    }

    private val orientationEventListener = object : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            // Doesn't support upside down orientation
            val rotation = when (orientation) {
                in 45 until 135 -> -90
                in 135 until 225 -> controlRotation
                in 225 until 315 -> 90
                else -> 0
            }

            if (controlRotation != rotation &&
                abs(deviceOrientation - orientation) > ORIENTATION_HYSTERESIS) {
                controlRotation = rotation
                deviceOrientation = orientation
                orientationAwareControls.map {
                    it.animate().rotation(rotation.toFloat()).start()
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

    fun animateCameraSwitchClick() {
        cameraSwitchButton.let {
            it.animate()
                .rotation(controlRotation - 360f)
                .withEndAction { it.rotation = controlRotation.toFloat() }
                .start()
        }
    }

    private companion object {
        const val ORIENTATION_HYSTERESIS = 10
    }
}
