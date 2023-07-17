package com.alfresco.content.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.component.databinding.ViewListUserRowBinding
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.getLocalizedName

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
internal class ListViewUserRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewListUserRowBinding.inflate(LayoutInflater.from(context), this)

    @ModelProp
    fun setData(dataObj: UserGroupDetails) {
        binding.tvUserInitial.text = context.getLocalizedName(dataObj.nameInitial)
        binding.tvName.text = dataObj.groupName.ifEmpty { context.getLocalizedName(dataObj.name ?: "") }
    }

    /**
     * set clickListener to the list item
     */
    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        binding.clUserParent.setOnClickListener(listener)
    }
}
