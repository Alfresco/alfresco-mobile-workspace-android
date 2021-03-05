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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        val pager = view.findViewById<ViewPager>(R.id.pager)
        pager.adapter = PagerAdapter(requireContext(), childFragmentManager)
        return view
    }

    private class PagerAdapter(val context: Context, fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> BrowseFragment.withArg(context.getString(R.string.nav_path_favorites))
                else -> BrowseFragment.withArg(context.getString(R.string.nav_path_fav_libraries))
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int) =
            when (position) {
                0 -> context.getString(R.string.favorites_tab_files)
                else -> context.getString(R.string.favorites_tab_libraries)
            }
    }
}
