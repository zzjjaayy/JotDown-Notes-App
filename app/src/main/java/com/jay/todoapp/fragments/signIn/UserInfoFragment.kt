package com.jay.todoapp.fragments.signIn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        // Inflate the layout for this fragment
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        if(mAuth.currentUser == null) {
            binding.userName.text = "idk mate"
            binding.userMail.text = "idk mate"
        } else {
            Glide.with(this).load(mAuth.currentUser?.photoUrl).into(binding.imageView2)
            binding.userName.text = mAuth.currentUser?.displayName
            binding.userMail.text = mAuth.currentUser?.email
        }
        binding.button2.setOnClickListener {
            mAuth.signOut()
        }
    }
}