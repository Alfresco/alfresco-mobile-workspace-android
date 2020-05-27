package com.alfresco.content.search

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.models.ResultNode
import kotlinx.android.synthetic.main.view_search_result_row.view.icon
import kotlinx.android.synthetic.main.view_search_result_row.view.path
import kotlinx.android.synthetic.main.view_search_result_row.view.properties
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
    fun setNode(node: ResultNode) {
        title.text = node.name
        icon.setImageDrawable(icon(node))
        properties.text = properties(node)
        path.text = path(node)
    }

    @CallbackProp
    fun setClickListener(listener: View.OnClickListener?) {
        setOnClickListener(listener)
    }

    private fun icon(node: ResultNode): Drawable {
        val type = if (node.isFolder) MimeType.FOLDER else MimeType.fromFilename(node.name)
        return resources.getDrawable(type.icon, null)
    }

    private fun properties(node: ResultNode): String {
        val modifiedAt = node.modifiedAt.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        val modifiedBy = node.modifiedByUser.displayName
        return "Modified: $modifiedAt by $modifiedBy"
    }

    private fun path(node: ResultNode): String? {
        return node.path?.elements
            ?.map { element -> element.name }
            ?.reduce { sum, element -> "$sum > $element" }
    }
}
