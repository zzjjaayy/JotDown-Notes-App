package com.jay.todoapp.onboarding.splashScreen

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.jay.todoapp.R
import com.jay.todoapp.ToDoSharedViewModel


class SplashFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        Handler(Looper.getMainLooper()).postDelayed({
            if(startOnBoarding()) {
                if(currentUser == null && startSignIn()) {
                    findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_listFragment)
                }
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
            }
          }, 1000)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    private fun startOnBoarding() : Boolean{
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("Finished", false)
    }
    private fun startSignIn() : Boolean{
        val sharedPref = requireActivity().getSharedPreferences("SignIn", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("SignIn", true)
    }
}