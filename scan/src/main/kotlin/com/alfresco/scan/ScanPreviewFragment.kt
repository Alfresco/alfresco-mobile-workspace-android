package com.alfresco.scan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.content.viewer.pdf.PdfPreviewProvider
import com.alfresco.pdf.CreatePdfDialog
import com.alfresco.scan.databinding.FragmentScanPreviewBinding
import com.alfresco.ui.getDrawableForAttribute

/**
 * Marked as ScanPreviewFragment class
 */
class ScanPreviewFragment : Fragment(), MavericksView {

    private val viewModel: ScanViewModel by activityViewModel()
    private lateinit var binding: FragmentScanPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as ScanActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        binding.toolbar.apply {
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { goBack() }
        }
    }

    private fun configureViewer(scanItem: ScanItem) {
        binding.toolbar.title = scanItem.name
        binding.title.text = scanItem.name
        val type = MimeType.with(scanItem.mimeType)
        binding.icon.setImageDrawable(
            ResourcesCompat.getDrawable(resources, type.icon, requireContext().theme)
        )

        val fragment = createViewer(scanItem.mimeType)
        if (fragment != null) {
            binding.apply {
                info.isVisible = fragment.showInfoWhenLoaded() == true
                status.text = ""
            }
            val childArgs = ChildViewerArgs(scanItem.uri.toString(), scanItem.mimeType)
            fragment.arguments = bundleOf(Mavericks.KEY_ARG to childArgs)

            childFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment, tag)
                .commit()
        } else {
            binding.apply {
                info.isVisible = true
                status.text = getString(R.string.error_preview_not_available)
            }
        }
    }

    private fun createViewer(mimeType: String) =
        when {
            PdfPreviewProvider.isMimeTypeSupported(mimeType) -> PdfPreviewProvider
            else -> null
        }?.createViewer()

    private fun goBack() {
        requireActivity().onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_scan, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                showPdfNameDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showPdfNameDialog() = withState(viewModel) { state ->
        val scanItem = state.listPdf[0]
        CreatePdfDialog.Builder(requireContext(), scanItem.name)
            .onSuccess { title, _ ->
                val activity = requireActivity()
                val list = ArrayList<ScanItem>(listOf(scanItem.copy(name = title)))
                val intent = Intent().apply { putParcelableArrayListExtra(ScanResultContract.OUTPUT_KEY, list) }
                activity.setResult(Activity.RESULT_OK, intent)
                activity.finish()
            }

            .onCancel {
            }
            .show()
    }

    override fun invalidate(): Unit = withState(viewModel) {
        if (it.listPdf.isNotEmpty()) {
            configureViewer(it.listPdf[0])
            showPdfNameDialog()
        }
    }
}
