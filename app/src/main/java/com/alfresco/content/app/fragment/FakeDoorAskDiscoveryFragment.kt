package com.alfresco.content.app.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.app.R
import com.alfresco.content.app.databinding.FragmentFakeDoorAskDiscoveryBinding
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.EventName
import com.alfresco.content.data.PageView
import kotlinx.parcelize.Parcelize

@Parcelize
data class FakeDoorAskDiscoveryArgs(
    val path: String,
) : Parcelable {

    companion object {
        private const val PATH_KEY = "path"

        /**
         * return the FakeDoorAskDiscoveryArgs obj
         */
        fun with(args: Bundle): FakeDoorAskDiscoveryArgs {
            return FakeDoorAskDiscoveryArgs(
                args.getString(PATH_KEY, ""),
            )
        }
    }
}

class FakeDoorAskDiscoveryFragment : Fragment(), MavericksView {

    private val viewModel: AskDiscoveryViewModel by fragmentViewModel()
    private lateinit var binding: FragmentFakeDoorAskDiscoveryBinding

    override fun onStart() {
        super.onStart()
        requireActivity().title = resources.getString(R.string.nav_title_ask_an_agent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFakeDoorAskDiscoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        binding.btnClose.setOnClickListener {
            requireActivity().finish()
        }
        binding.iconLike.setOnClickListener {
            binding.actionMessage.visibility = View.VISIBLE
            binding.bgLike.visibility = View.VISIBLE
            binding.iconLike.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.alfresco_blue_900))
            AnalyticsManager().commonEvent(EventName.AskDiscoveryLike)

        }
        binding.iconDislike.setOnClickListener {
            binding.actionMessage.visibility = View.VISIBLE
            binding.bgDislike.visibility = View.VISIBLE
            binding.iconDislike.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.alfresco_blue_900))
            AnalyticsManager().commonEvent(EventName.AskDiscoveryDislike)
        }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsManager().screenViewEvent(PageView.FakeDoorAskDiscovery)
    }

    override fun invalidate() = withState(viewModel) {

    }
}