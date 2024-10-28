package com.alfresco.content.actions

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.actions.databinding.ViewProcessDefinitionsListRowBinding
import com.alfresco.content.data.RuntimeProcessDefinitionDataEntry

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
internal class ListRowProcessDefinitions
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val binding = ViewProcessDefinitionsListRowBinding.inflate(LayoutInflater.from(context), this, true)

        @ModelProp(options = [ModelProp.Option.IgnoreRequireHashCode])
        fun setProcessDefinition(data: RuntimeProcessDefinitionDataEntry) {
            binding.apply {
                title.text = data.name
                subtitle.text = data.description
            }
        }

        @CallbackProp
        fun setClickListener(listener: OnClickListener?) {
            setOnClickListener(listener)
        }
    }
