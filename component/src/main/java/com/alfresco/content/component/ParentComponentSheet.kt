package com.alfresco.content.component

import com.airbnb.mvrx.MavericksView
import com.alfresco.ui.BottomSheetDialogFragment

abstract class ParentComponentSheet : BottomSheetDialogFragment(), MavericksView {

    var onApply: ComponentApplyCallback? = null
    var onReset: ComponentResetCallback? = null
    var onCancel: ComponentCancelCallback? = null
}
