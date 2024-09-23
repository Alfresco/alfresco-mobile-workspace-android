package com.alfresco.content.listview

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.listview.databinding.ViewListRowBinding
import com.alfresco.content.mimetype.MimeType
import com.alfresco.ui.getDrawableForAttribute

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListRowBinding.inflate(LayoutInflater.from(context), this, true)
    private var isCompact: Boolean = false
    private lateinit var entry: Entry
    private var multiSelectionEnabled = false
    private var menuActionsEnabled = false

    @ModelProp
    fun setData(entry: Entry) {
        this.entry = entry
        binding.title.text = entry.name
        binding.subtitle.text = entry.path
        updateSubtitleVisibility()

        val type = when (entry.type) {
            Entry.Type.SITE -> MimeType.LIBRARY
            Entry.Type.FOLDER -> MimeType.FOLDER
            Entry.Type.FILE_LINK -> MimeType.FILE_LINK
            Entry.Type.FOLDER_LINK -> MimeType.FOLDER_LINK
            else -> MimeType.with(entry.mimeType)
        }

        if (entry.isExtension && !entry.isFolder) {
            binding.parent.alpha = 0.5f
            binding.parent.isEnabled = false
        } else {
            binding.parent.alpha = 1.0f
            binding.parent.isEnabled = true
        }

        binding.moreButton.isEnabled = !entry.isExtension

        binding.icon.setImageDrawable(ResourcesCompat.getDrawable(resources, type.icon, context.theme))

        val accessibilityText = if (entry.path.isNullOrEmpty()) {
            context.getString(
                R.string.accessibility_text_title,
                entry.name,
            )
        } else context.getString(
            R.string.accessibility_text_simple_row,
            entry.name,
            entry.path,
        )
        binding.parent.contentDescription = accessibilityText
    }

    private fun configureOfflineStatus(entry: Entry) {
        // Outside offline screen
        if (entry.isOffline && !entry.hasOfflineStatus) {
            binding.offlineIcon.isVisible = true
            binding.offlineIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_offline_marked,
                    context.theme,
                ),
            )
        } else {
            // Offline screen items and uploads
            if (entry.isFile && entry.hasOfflineStatus) {
                val config = makeOfflineStatusConfig(entry)
                val drawableRes = config.first
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

                val stringRes = config.second
                if (stringRes != null) {
                    binding.subtitle.text = context.getString(stringRes)
                }
            } else {
                binding.offlineIcon.isVisible = false
            }
        }
    }

    private fun makeOfflineStatusConfig(entry: Entry): Pair<Int?, Int?> =
        when (entry.offlineStatus) {
            OfflineStatus.PENDING ->
                if (entry.isUpload) {
                    Pair(R.drawable.ic_offline_upload, null)
                } else {
                    Pair(R.drawable.ic_offline_status_pending, null)
                }

            OfflineStatus.SYNCING ->
                Pair(R.drawable.ic_offline_status_in_progress_anim, R.string.offline_status_in_progress)

            OfflineStatus.SYNCED ->
                Pair(R.drawable.ic_offline_status_synced, null)

            OfflineStatus.ERROR ->
                Pair(R.drawable.ic_offline_status_error, R.string.offline_status_error)

            else ->
                Pair(R.drawable.ic_offline_status_synced, null)
        }

    private fun actionButtonVisibility(entry: Entry) =
        !entry.isLink && !entry.isUpload &&
            // Child folder in offline tab
            !(entry.isFolder && entry.hasOfflineStatus && !entry.isOffline)

    @ModelProp
    fun setCompact(compact: Boolean) {
        this.isCompact = compact

        val heightResId = if (compact) R.dimen.list_row_compact_height else R.dimen.list_row_height
        binding.parent.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimension(heightResId).toInt(),
        )

        updateSubtitleVisibility()
    }

    private fun updateSubtitleVisibility() {
        binding.subtitle.isVisible = binding.subtitle.text.isNotEmpty() && !isCompact
    }

    @ModelProp
    fun setMultiSelection(isMultiSelection: Boolean) {
        this.multiSelectionEnabled = isMultiSelection
    }

    @ModelProp
    fun setMenuAction(enabled: Boolean) {
        menuActionsEnabled = enabled
    }

    @AfterPropsSet
    fun bind() {
        binding.checkBox.isChecked = entry.isSelectedForMultiSelection

        if (entry.isSelectedForMultiSelection) {
            binding.parent.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackgroundMultiSelection))
        } else {
            binding.parent.background = context.getDrawableForAttribute(R.attr.selectableItemBackground)
        }

        if (multiSelectionEnabled) {
            binding.moreButton.isVisible = false
            binding.offlineIcon.isVisible = false
            binding.checkBox.isVisible = true
        } else {
            postDataSet()
        }
    }

    private fun postDataSet() {
        binding.checkBox.isVisible = false
        binding.checkBox.isChecked = false

        binding.moreButton.isEnabled = menuActionsEnabled
        binding.moreButton.isVisible = actionButtonVisibility(entry)
        configureOfflineStatus(entry)
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        binding.parent.setOnClickListener(listener)
    }

    /**
     * long press gesture for the row
     */
    @CallbackProp
    fun setLongClickListener(listener: OnLongClickListener?) {
        binding.parent.setOnLongClickListener(listener)
    }

    @CallbackProp
    fun setMoreClickListener(listener: OnClickListener?) {
        binding.moreButton.setOnClickListener(listener)
    }
}
