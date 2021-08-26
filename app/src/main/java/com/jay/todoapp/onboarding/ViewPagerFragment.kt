package com.jay.todoapp.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.jay.todoapp.R
import com.jay.todoapp.onboarding.screens.FirstOnboardScreen
import com.jay.todoapp.onboarding.screens.SecondOnboardScreen
import com.jay.todoapp.onboarding.screens.ThirdOnboardScreen

class ViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)
        val fragmentList = arrayListOf<Fragment>(
            FirstOnboardScreen(), SecondOnboardScreen(), ThirdOnboardScreen()
        )
        val adapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )
        view.findViewById<ViewPager2>(R.id.view_pager).adapter = adapter
        return view
    }

}