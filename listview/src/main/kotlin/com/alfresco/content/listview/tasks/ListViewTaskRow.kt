package com.alfresco.content.listview.tasks

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.data.Entry
import com.alfresco.content.listview.R
import com.alfresco.content.listview.databinding.ViewListTaskRowBinding
import com.alfresco.content.mimetype.MimeType

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewTaskRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListTaskRowBinding.inflate(LayoutInflater.from(context), this, true)
    private var isCompact: Boolean = false

    @ModelProp
    fun setData(entry: Entry) {
        binding.title.text = entry.name
        binding.subtitle.text = entry.path
        updateSubtitleVisibility()

        val type = when (entry.type) {
            Entry.Type.SITE -> MimeType.LIBRARY
            Entry.Type.FOLDER -> MimeType.FOLDER
            Entry.Type.FILE_LINK -> MimeType.FILE_LINK
            Entry.Type.FOLDER_LINK -> MimeType.FOLDER_LINK
            else -> MimeType.with(entry.mimeType)
        }

        if (entry.isExtension && !entry.isFolder) {
            binding.parent.alpha = 0.5f
            binding.parent.isEnabled = false
        } else {
            binding.parent.alpha = 1.0f
            binding.parent.isEnabled = true
        }
    }

    @ModelProp
    fun setCompact(compact: Boolean) {
        this.isCompact = compact

        val heightResId = if (compact) R.dimen.list_row_compact_height else R.dimen.list_row_height
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimension(heightResId).toInt()
        )

        updateSubtitleVisibility()
    }

    private fun updateSubtitleVisibility() {
        binding.subtitle.isVisible = binding.subtitle.text.isNotEmpty() && !isCompact
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }

    @CallbackProp
    fun setMoreClickListener(listener: OnClickListener?) {
    }
}
