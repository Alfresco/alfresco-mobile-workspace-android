package com.alfresco.content.viewer

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.withState
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.viewer.common.ViewerTypeArgs
import com.alfresco.content.viewer.image.ImageViewerFragment
import com.alfresco.content.viewer.media.MediaViewerFragment
import com.alfresco.content.viewer.pdf.PdfViewerFragment
import com.alfresco.content.viewer.text.TextViewerFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ViewerArgs(
    val id: String,
    val title: String,
    val type: String
) : Parcelable {
    companion object {
        private const val ID_KEY = "id"
        private const val TITLE_KEY = "title"
        private const val TYPE_KEY = "type"

        fun with(args: Bundle): ViewerArgs {
            return ViewerArgs(
                args.getString(ID_KEY, ""),
                args.getString(TITLE_KEY, ""),
                args.getString(TYPE_KEY, "")
            )
        }
    }
}

class ViewerFragment : BaseMvRxFragment(R.layout.viewer) {

    private lateinit var args: ViewerArgs
    private val viewModel: ViewerViewModel by fragmentViewModelWithArgs { args }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = ViewerArgs.with(requireArguments())
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.ready) {
            if (state.viewerType != null) {
                if (childFragmentManager.findFragmentByTag(state.viewerType.toString()) == null) {
                    val fragment = viewerFragment(state.viewerType, typeArgs(state.viewerUri ?: ""))
                    childFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment, state.viewerType.toString())
                        .commit()
                }
            } else {
                showError(getString(R.string.error_preview_not_available))
            }
        }
    }

    private fun typeArgs(uri: String): ViewerTypeArgs {
        return ViewerTypeArgs(args.id, uri, args.type)
    }

    private fun viewerFragment(type: ViewerType, args: ViewerTypeArgs): Fragment {
        return when (type) {
            ViewerType.Pdf -> PdfViewerFragment()
            ViewerType.Image -> ImageViewerFragment()
            ViewerType.Text -> TextViewerFragment()
            ViewerType.Media -> MediaViewerFragment()
        }.apply { arguments = bundleOf(MvRx.KEY_ARG to args) }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
}
