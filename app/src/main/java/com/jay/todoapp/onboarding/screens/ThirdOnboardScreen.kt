package com.jay.todoapp.onboarding.screens

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.jay.todoapp.R
import com.jay.todoapp.onboarding.splashScreen.SplashFragment

class ThirdOnboardScreen : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_third_onboard_screen, container, false)
        view.findViewById<ExtendedFloatingActionButton>(R.id.next_fab).setOnClickListener {
            onBoardingFinished()
            if(startSignIn()) {
                findNavController().navigate(R.id.action_viewPagerFragment_to_signInFragment)
            } else {
                findNavController().navigate(R.id.action_viewPagerFragment_to_listFragment)
            }
        }
        return view
    }

    private fun onBoardingFinished() {
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }

    private fun startSignIn() : Boolean{
        val sharedPref = requireActivity().getSharedPreferences("SignIn", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("SignIn", true)
    }
}