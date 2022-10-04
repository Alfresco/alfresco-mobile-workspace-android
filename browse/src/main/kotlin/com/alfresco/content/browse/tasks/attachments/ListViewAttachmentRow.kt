package com.alfresco.content.browse.tasks.attachments

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.browse.databinding.ViewListAttachmentRowBinding
import com.alfresco.content.data.ContentEntry
import com.alfresco.content.mimetype.MimeType

/**
 * Marked as ListViewAttachmentRow class
 */
@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewAttachmentRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListAttachmentRowBinding.inflate(LayoutInflater.from(context), this)

    /**
     * set the content data on list row
     */
    @ModelProp
    fun setData(data: ContentEntry) {
        binding.tvName.text = data.name
        binding.iconFile.setImageDrawable(ResourcesCompat.getDrawable(resources, MimeType.with(data.mimeType).icon, context.theme))
    }

    /**
     * list row click listener
     */
    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }

    /**
     * delete icon click listener
     */
    @CallbackProp
    fun setDeleteContentClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }
}
