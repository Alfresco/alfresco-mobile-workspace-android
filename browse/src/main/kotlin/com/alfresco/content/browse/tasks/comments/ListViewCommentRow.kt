package com.alfresco.content.browse.tasks.comments

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_4
import com.alfresco.content.browse.databinding.ViewListCommentRowBinding
import com.alfresco.content.data.CommentEntry
import com.alfresco.content.getFormattedDate
import com.alfresco.content.getLocalizedName

/**
 * Marked as ListViewCommentRow class
 */
@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewCommentRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListCommentRowBinding.inflate(LayoutInflater.from(context), this)

    /**
     * set the comment row on list row
     */
    @ModelProp
    fun setData(data: CommentEntry) {
        binding.tvName.text = context.getLocalizedName(data.userGroupDetails?.name ?: "")
        binding.tvUserInitial.text = context.getLocalizedName(data.userGroupDetails?.nameInitial ?: "")
        binding.tvComment.text = data.message
        binding.tvDate.text = if (data.created != null) data.created?.toLocalDate().toString().getFormattedDate(DATE_FORMAT_1, DATE_FORMAT_4) else ""
    }
}
