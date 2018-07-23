package com.perusdajepara.jadsqrcode.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class MainTabAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    val titleList = ArrayList<String>()
    val fragmentList = ArrayList<Fragment>()

    fun addFragment(title: String, fragment: Fragment) {
        titleList.add(title)
        fragmentList.add(fragment)
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titleList[position]
    }

}