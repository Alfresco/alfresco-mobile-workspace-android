package com.alfresco.content.viewer

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.airbnb.mvrx.BaseMvRxFragment
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
    // private val viewModel: ViewerViewModel by fragmentViewModelWithArgs { args }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = ViewerArgs.with(requireArguments())
        childFragmentManager.fragmentFactory = ViewerFragmentFactory(args.id, args.type)

        val classLoader = ViewerFragmentFactory::class.java.classLoader!!
        val fragment = when {
            args.type.startsWith("text/") -> childFragmentManager.fragmentFactory.instantiate(classLoader, TextViewerFragment::class.java.name)
            args.type.startsWith("image/") -> childFragmentManager.fragmentFactory.instantiate(classLoader, ImageViewerFragment::class.java.name)
            else -> childFragmentManager.fragmentFactory.instantiate(classLoader, PdfViewerFragment::class.java.name)
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }

    override fun invalidate() {
    }
}

class ViewerFragmentFactory(
    private val documentId: String,
    private val mimeType: String
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            PdfViewerFragment::class.java.name -> PdfViewerFragment(documentId, mimeType)
            TextViewerFragment::class.java.name -> TextViewerFragment(documentId, mimeType)
            ImageViewerFragment::class.java.name -> ImageViewerFragment(documentId, mimeType)
            else -> super.instantiate(classLoader, className)
        }
    }
}
