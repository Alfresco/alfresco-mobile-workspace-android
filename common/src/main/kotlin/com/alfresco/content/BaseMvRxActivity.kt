package com.alfresco.content

import androidx.appcompat.app.AppCompatActivity
import com.airbnb.mvrx.MvRxView

/**
 * Base class for supporting MvRx activities similar to [com.airbnb.mvrx.BaseMvRxFragment]
 */
@Deprecated("You no longer need a base MvRxFragment. All you need to do is make your Fragment implement MvRxView.")
abstract class BaseMvRxActivity : AppCompatActivity(), MvRxView
