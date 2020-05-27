package com.alfresco.content.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.models.ResultSetRowEntry
import kotlinx.android.synthetic.main.view_search_result_row.view.icon
import kotlinx.android.synthetic.main.view_search_result_row.view.subtitle
import kotlinx.android.synthetic.main.view_search_result_row.view.title
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class SearchResultRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_search_result_row, this, true)
    }

    @ModelProp
    fun setEntry(entry: ResultSetRowEntry) {
        title.text = entry.entry.name
        val type = if (entry.entry.isFolder) MimeType.FOLDER else MimeType.fromFilename(entry.entry.name)
        icon.setImageDrawable(resources.getDrawable(type.icon, null))
        subtitle.text = "Modified: " + entry.entry.modifiedAt.format(
            DateTimeFormatter.ofLocalizedDate(
                FormatStyle.MEDIUM)) + " by " + entry.entry.modifiedByUser.displayName
    }

    @CallbackProp
    fun setClickListener(listener: View.OnClickListener?) {
        setOnClickListener(listener)
    }
}