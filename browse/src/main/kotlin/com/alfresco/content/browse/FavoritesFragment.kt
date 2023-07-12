package com.alfresco.content.browse

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager

class FavoritesFragment : Fragment() {

    private lateinit var pager: ViewPager
    private lateinit var pagerAdapter: PagerAdapter
    private var listFragments = mutableListOf<Fragment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        pager = view.findViewById(R.id.pager)
        listFragments = mutableListOf(
            BrowseFragment.withArg(requireContext().getString(R.string.nav_path_favorites)),
            BrowseFragment.withArg(requireContext().getString(R.string.nav_path_fav_libraries))
        )
        pagerAdapter = PagerAdapter(requireContext(), childFragmentManager, listFragments)
        pager.adapter = pagerAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        view.requestFocus()
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

    fun clearMultiSelection() {
        val fragment = listFragments[pager.currentItem]
        if (fragment is BrowseFragment) {
            fragment.clearMultiSelection()
        }
    }
}
