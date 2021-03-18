package com.alfresco.content

import com.airbnb.mvrx.MvRxView
import com.alfresco.ui.BottomSheetDialogFragment

/**
 * Base class for supporting MvRx bottom sheets similar to [com.airbnb.mvrx.BaseMvRxFragment]
 */
@Deprecated("You no longer need a base MvRxFragment. All you need to do is make your Fragment implement MvRxView.")
abstract class BaseMvRxBottomSheet : BottomSheetDialogFragment(), MvRxView
