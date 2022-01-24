package com.example.chatapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatapp.viewpagerFragments.GroupsFragment
import com.example.chatapp.viewpagerFragments.UsersFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity, val list: ArrayList<Int>): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return if (list[position] == 1) {
            UsersFragment()
        } else GroupsFragment()
    }
}