package com.example.sawit.IsiDashboard

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.sawit.ScreenActivity

class ScreenAdapter (
    activity: ScreenActivity,
    private val fragments: List<Fragment>
)  : FragmentStateAdapter(activity){
    override fun getItemCount(): Int {
        return fragments.size
    }
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}