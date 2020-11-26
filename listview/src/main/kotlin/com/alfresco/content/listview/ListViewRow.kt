package com.alfresco.content.listview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.data.Entry
import com.alfresco.content.mimetype.MimeType
import kotlinx.android.synthetic.main.view_list_row.view.icon
import kotlinx.android.synthetic.main.view_list_row.view.more_icon_frame
import kotlinx.android.synthetic.main.view_list_row.view.subtitle
import kotlinx.android.synthetic.main.view_list_row.view.title

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var isCompact: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_list_row, this, true)
    }

    @ModelProp
    fun setData(entry: Entry) {
        title.text = entry.title
        subtitle.text = entry.subtitle
        updateSubtitleVisibility()

        val type = when (entry.type) {
            Entry.Type.Site -> MimeType.LIBRARY
            Entry.Type.Folder -> MimeType.FOLDER
            Entry.Type.FileLink -> MimeType.FILE_LINK
            Entry.Type.FolderLink -> MimeType.FOLDER_LINK
            else -> MimeType.with(entry.mimeType)
        }

        icon.setImageDrawable(ResourcesCompat.getDrawable(resources, type.icon, context.theme))
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
        subtitle.isVisible = subtitle.text.isNotEmpty() && !isCompact
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }

    @CallbackProp
    fun setMoreClickListener(listener: OnClickListener?) {
        more_icon_frame.setOnClickListener(listener)
    }
}
