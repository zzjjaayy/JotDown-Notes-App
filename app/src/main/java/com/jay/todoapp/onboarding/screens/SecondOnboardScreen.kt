package com.jay.todoapp.onboarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.jay.todoapp.R

class SecondOnboardScreen : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_second_onboard_screen, container, false)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.view_pager)
        view.findViewById<ExtendedFloatingActionButton>(R.id.next_fab).setOnClickListener {
            viewPager?.currentItem = 2
        }
        return view
    }
}