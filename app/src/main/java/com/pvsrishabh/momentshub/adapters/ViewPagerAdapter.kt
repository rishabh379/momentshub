package com.pvsrishabh.momentshub.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager, private val uid: String): FragmentPagerAdapter(fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    private val fragmentList = mutableListOf<Fragment>()
    private val titleList = mutableListOf<String>()

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): String {
        return titleList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragment.arguments = Bundle().apply {
            putString("uid", uid)
        }
        fragmentList.add(fragment)
        titleList.add(title)
    }

}