package com.alfresco.content.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.models.CategoriesItem
import com.alfresco.content.search.databinding.ViewListFilterChipsBinding

/**
 * Generated Model View for the Advance Filter Chips
 */
@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ListViewFilterChips @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListFilterChipsBinding.inflate(LayoutInflater.from(context), this)


    /**
     * Bind the capture item data to the view
     */
    @ModelProp
    fun setData(item: String) {
        binding.chipContextual.text=item
    }

    /**
     * set clickListener to the list item
     */
    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }


}
