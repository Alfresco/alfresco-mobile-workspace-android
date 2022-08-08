package com.alfresco.content.browse.tasks.detail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.browse.databinding.ViewListCommentRowBinding
import com.alfresco.content.data.CommentEntry

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class ListViewCommentRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListCommentRowBinding.inflate(LayoutInflater.from(context), this)

    @ModelProp
    fun setData(data: CommentEntry) {
        println("ListViewCommentRow.setData $data")
        binding.tvName.text = data.userDetails?.name
        binding.tvComment.text = data.message
    }
}
