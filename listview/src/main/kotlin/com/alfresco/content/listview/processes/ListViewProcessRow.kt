package com.alfresco.content.listview.processes

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.DATE_FORMAT_6
import com.alfresco.content.DATE_FORMAT_7
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.getLocalFormattedDate
import com.alfresco.content.getLocalizedName
import com.alfresco.content.listview.R
import com.alfresco.content.listview.databinding.ViewListProcessRowBinding

/**
 * Marked as ListViewProcessRow class
 */
@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewProcessRow
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val binding = ViewListProcessRowBinding.inflate(LayoutInflater.from(context), this, true)

        /**
         * set the data on view
         */
        @RequiresApi(Build.VERSION_CODES.O)
        @ModelProp
        fun setData(entry: ProcessEntry) {
            binding.title.text = entry.name
            val localizedName = context.getLocalizedName(entry.startedBy?.name ?: "")
            binding.subtitle.text = localizedName
            binding.timeStamp.text = entry.started?.toLocalDateTime().toString().getLocalFormattedDate(DATE_FORMAT_6, DATE_FORMAT_7)
            val accessibilityText =
                context.getString(
                    R.string.accessibility_text_process_row,
                    entry.name,
                    localizedName,
                )
            binding.parent.contentDescription = accessibilityText
        }

        /**
         * row click listener
         */
        @CallbackProp
        fun setClickListener(listener: OnClickListener?) {
            setOnClickListener(listener)
        }
    }
