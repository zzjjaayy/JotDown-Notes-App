package com.jay.todoapp.fragments.signIn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.jay.todoapp.R
import com.jay.todoapp.databinding.FragmentUserInfoBinding

class UserInfoFragment : Fragment() {

    private var _binding : FragmentUserInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        // Inflate the layout for this fragment
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        if(mAuth.currentUser == null) {
            binding.apply {
                userName.visibility = View.GONE
                userMail.visibility = View.GONE
                userImage.visibility = View.GONE
                sectionDivider.visibility = View.GONE
//                backupOptionTitle.visibility = View.GONE
                logButton.text = "Sign In"
                logButton.setIconResource(R.drawable.ic_google_logo)
                logButton.setOnClickListener {
                    findNavController().navigate(R.id.action_userInfoFragment_to_signInFragment)
                }
            }
        } else {
            Glide.with(this).load(mAuth.currentUser?.photoUrl).circleCrop().into(binding.userImage)
            binding.apply {
                userName.text = mAuth.currentUser?.displayName
                userMail.text = mAuth.currentUser?.email
                logButton.text = "Sign Out"
                logButton.setIconResource(R.drawable.ic_logout)
                logButton.setOnClickListener {
                    mAuth.signOut()
                    findNavController().popBackStack()
                }
            }
        }
    }
}