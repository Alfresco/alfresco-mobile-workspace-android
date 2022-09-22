package com.alfresco.content.browse.tasks.detail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.browse.databinding.ViewListAttachmentRowBinding
import com.alfresco.content.data.ContentEntry

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
    }
}