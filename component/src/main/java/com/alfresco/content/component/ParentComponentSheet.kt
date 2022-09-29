package com.alfresco.content.component

import com.airbnb.mvrx.MavericksView
import com.alfresco.ui.BottomSheetDialogFragment

/**
 * Marked as ParentComponentSheet class
 */
abstract class ParentComponentSheet : BottomSheetDialogFragment(), MavericksView {

    var onApply: ComponentApplyCallback? = null
    var onReset: ComponentResetCallback? = null
    var onCancel: ComponentCancelCallback? = null
}
