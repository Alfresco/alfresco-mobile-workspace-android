package com.alfresco.content.viewer

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.withState
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.viewer.image.ImageViewerFragment
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
    private val fragmentFactory = ViewerFragmentFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = fragmentFactory

        super.onCreate(savedInstanceState)

        args = ViewerArgs.with(requireArguments())
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.ready) {
            fragmentFactory.id = state.id
            fragmentFactory.uri = state.viewerUri ?: ""

            if (state.viewerType != null) {
                val fragment = viewerFragment(state.viewerType)
                childFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment)
                    .commit()
            } else {
                showError(getString(R.string.error_preview_not_available))
            }
        }
    }

    private fun viewerFragment(type: ViewerType): Fragment {
        val classLoader = ViewerFragmentFactory::class.java.classLoader!!
        return when (type) {
            ViewerType.Pdf -> childFragmentManager.fragmentFactory.instantiate(classLoader, PdfViewerFragment::class.java.name)
            ViewerType.Image -> childFragmentManager.fragmentFactory.instantiate(classLoader, ImageViewerFragment::class.java.name)
            ViewerType.Text -> childFragmentManager.fragmentFactory.instantiate(classLoader, TextViewerFragment::class.java.name)
        }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
}

class ViewerFragmentFactory() : FragmentFactory() {

    var id: String = ""
    var uri: String = ""

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            PdfViewerFragment::class.java.name -> PdfViewerFragment(uri)
            TextViewerFragment::class.java.name -> TextViewerFragment("documentId", "mimeType")
            ImageViewerFragment::class.java.name -> ImageViewerFragment(id, uri)
            else -> super.instantiate(classLoader, className)
        }
    }
}
