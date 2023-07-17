package com.alfresco.capture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView

class ShutterButton(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val background = createBackground()
    private val imageView = createImageView()

    var state: State = State.Photo
        set(value) {
            updateImage(field, value)
            field = value
        }

    constructor(context: Context) :
        this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, 0)

    init {
        addView(background)
        addView(imageView)
    }

    private fun createImageView() =
        ImageView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
            )
            setImageResource(R.drawable.ic_shutter_photo_to_video)
            // Seems that the drawable is sometimes loaded in the end state, so reset it.
            (drawable as? AnimatedVectorDrawable)?.reset()
        }

    private fun createBackground() =
        ImageView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
            )
            setImageResource(R.drawable.bg_shutter_btn)
        }

    private fun updateImage(old: State, new: State) {
        if (old == new) return

        val resId = when {
            old == State.Photo && new == State.Video -> R.drawable.ic_shutter_photo_to_video
            old == State.Video && new == State.Photo -> R.drawable.ic_shutter_video_to_photo
            old == State.Video && new == State.Recording -> R.drawable.ic_shutter_video_to_record
            old == State.Recording && new == State.Video -> R.drawable.ic_shutter_record_to_video
            else -> throw IllegalStateException()
        }

        imageView.setImageResource(resId)
        (imageView.drawable as? AnimatedVectorDrawable)?.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (state == State.Photo) {
            if (event?.action == MotionEvent.ACTION_DOWN) {
                imageView.animate()
                    .scaleX(ON_PRESS_SCALE_RATIO)
                    .scaleY(ON_PRESS_SCALE_RATIO)
                    .setDuration(ON_PRESS_ANIM_DURATION)
                    .start()
            } else if (event?.action == MotionEvent.ACTION_UP ||
                event?.action == MotionEvent.ACTION_CANCEL
            ) {
                imageView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(ON_PRESS_ANIM_DURATION)
                    .start()
            }
        }

        return super.onTouchEvent(event)
    }

    enum class State {
        Photo,
        Video,
        Recording,
    }

    private companion object {
        const val ON_PRESS_ANIM_DURATION = 100L
        const val ON_PRESS_SCALE_RATIO = 0.7f
    }
}
