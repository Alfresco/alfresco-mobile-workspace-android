package com.alfresco.content.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.component.databinding.ViewListUserRowBinding
import com.alfresco.content.data.UserDetails

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
internal class ListViewUserRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewListUserRowBinding.inflate(LayoutInflater.from(context), this)

    @ModelProp
    fun setData(dataObj: UserDetails) {
        binding.tvUserInitial.text = dataObj.nameInitial
        binding.tvName.text = dataObj.name
    }
}
