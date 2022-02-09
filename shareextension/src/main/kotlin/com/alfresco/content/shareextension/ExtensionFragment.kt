package com.alfresco.content.shareextension

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import kotlinx.parcelize.Parcelize

/**
 * Marked as ExtensionArgs class
 */
@Parcelize
data class ExtensionArgs(val image: String) : Parcelable {

    companion object {
        private const val PATH_KEY = "path"

        fun bundle(path: String) = bundleOf(PATH_KEY to path)
    }
}

/**
 * Marked as ExtensionFragment class
 */
class ExtensionFragment : Fragment(), MavericksView {

    private val viewModel: ExtensionViewModel by fragmentViewModel()

    override fun invalidate() = withState(viewModel) {
    }
}
