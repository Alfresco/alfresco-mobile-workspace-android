package com.alfresco.content.process.ui.epoxy

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.listview.R
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.process.databinding.ViewListAttachmentRowBinding

/**
 * Marked as ListViewAttachmentRow class
 */
@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewAttachmentRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListAttachmentRowBinding.inflate(LayoutInflater.from(context), this)

    /**
     * set the content data on list row
     */
    @ModelProp
    fun setData(data: Entry) {
        binding.tvName.text = data.name
        binding.iconFile.setImageDrawable(ResourcesCompat.getDrawable(resources, MimeType.with(data.mimeType).icon, context.theme))

        configureOfflineStatus(data)

        binding.deleteContentButton.visibility = if (actionButtonVisibility(data)) View.VISIBLE else View.INVISIBLE
    }

    private fun configureOfflineStatus(entry: Entry) {
        // Offline screen items and uploads
        if (entry.isFile && entry.hasOfflineStatus) {
            val drawableRes = makeOfflineStatusConfig(entry)
            if (drawableRes != null) {
                val drawable =
                    ResourcesCompat.getDrawable(resources, drawableRes, context.theme)
                if (drawable is AnimatedVectorDrawable) {
                    drawable.start()
                }
                binding.offlineIcon.setImageDrawable(drawable)
                binding.offlineIcon.isVisible = true
            } else {
                binding.offlineIcon.isVisible = false
            }
        } else {
            binding.offlineIcon.isVisible = false
        }
    }

    private fun makeOfflineStatusConfig(entry: Entry): Int? =
        when (entry.offlineStatus) {
            OfflineStatus.PENDING ->
                if (entry.isUpload) {
                    R.drawable.ic_offline_upload
                } else {
                    R.drawable.ic_offline_status_pending
                }

            OfflineStatus.SYNCING ->
                R.drawable.ic_offline_status_in_progress_anim

            OfflineStatus.SYNCED ->
                R.drawable.ic_offline_status_synced

            OfflineStatus.ERROR ->
                R.drawable.ic_offline_status_error

            else ->
                R.drawable.ic_offline_status_synced
        }

    private fun actionButtonVisibility(entry: Entry) =
        !entry.isLink && !entry.isUpload &&
            // Child folder in offline tab
            !(entry.isFolder && entry.hasOfflineStatus && !entry.isOffline) && !entry.isReadOnly

    /**
     * list row click listener
     */
    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }

    /**
     * delete icon click listener
     */
    @CallbackProp
    fun setDeleteContentClickListener(listener: OnClickListener?) {
        binding.deleteContentButton.setOnClickListener(listener)
    }
}
