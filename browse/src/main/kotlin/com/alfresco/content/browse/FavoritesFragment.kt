package com.alfresco.content.browse

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.alfresco.content.data.MultiSelection
import com.alfresco.content.listview.NonSwipeableViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    private lateinit var pager: NonSwipeableViewPager
    private lateinit var pagerAdapter: PagerAdapter
    private var listFragments = mutableListOf<Fragment>()
    private lateinit var tabs: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        pager = view.findViewById(R.id.pager)
        tabs = view.findViewById(R.id.tabs)
        listFragments =
            mutableListOf(
                BrowseFragment.withArg(requireContext().getString(R.string.nav_path_favorites)),
                BrowseFragment.withArg(requireContext().getString(R.string.nav_path_fav_libraries)),
            )
        pagerAdapter = PagerAdapter(requireContext(), childFragmentManager, listFragments)
        pager.adapter = pagerAdapter
        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        view.requestFocus()

        GlobalScope.launch {
            MultiSelection.observeClearSelection().collect {
                Handler(Looper.getMainLooper()).post {
                    if (isAdded) {
                        enableDisableTabs(it)
                    }
                }
            }
        }
    }

    private fun enableDisableTabs(isClearSelection: Boolean) {
        for (i in 0 until tabs.tabCount) {
            val tab = tabs.getTabAt(i)
            tab?.view?.isClickable = isClearSelection
        }
        pager.setSwipeEnabled(isClearSelection)
    }

    private class PagerAdapter(val context: Context, fragmentManager: FragmentManager, val listFragments: List<Fragment>) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> listFragments[position]
                else -> listFragments[position]
            }
        }

        override fun getCount(): Int {
            return listFragments.size
        }

        override fun getPageTitle(position: Int) =
            when (position) {
                0 -> context.getString(R.string.favorites_tab_files)
                else -> context.getString(R.string.favorites_tab_libraries)
            }
    }
}
