package com.jay.todoapp.fragments.signIn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.jay.todoapp.R
import com.jay.todoapp.databinding.FragmentUserInfoBinding
import com.jay.todoapp.utils.BiometricHelper
import com.jay.todoapp.utils.LOCK_ARCHIVE
import com.jay.todoapp.utils.SECURE_ARCHIVE

class UserInfoFragment : Fragment() {

    private var _binding : FragmentUserInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAuth: FirebaseAuth
    private lateinit var authHelper: BiometricHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        authHelper = BiometricHelper.getInstance(requireActivity())
        // Inflate the layout for this fragment
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        val isAuthAvailable = authHelper.isBioAuthAvailable()
        binding.apply {
            lockText.isVisible = isAuthAvailable
            lockSwitch.isVisible = isAuthAvailable
            if(isAuthAvailable) {
                lockSwitch.apply {
                    isChecked = getShouldLock()
                    setOnCheckedChangeListener { buttonView, isChecked ->
                        if(!isChecked && getShouldLock()) {
                            authHelper.bio {
                                binding.lockSwitch.isChecked = !it
                            }
                        } else setShouldLock(isChecked)
                    }
                }
            }
        }

        if(mAuth.currentUser == null) {
            binding.apply {
                userName.visibility = View.GONE
                userMail.visibility = View.GONE
                userImage.visibility = View.GONE
                sectionDivider.visibility = View.GONE
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
                    findNavController().navigate(R.id.action_userInfoFragment_to_signInFragment)
                }
            }
        }
    }

    private fun getShouldLock() = requireActivity()
        .getSharedPreferences(SECURE_ARCHIVE, Context.MODE_PRIVATE).getBoolean(LOCK_ARCHIVE, false)

    private fun setShouldLock(shouldLock: Boolean) {
        requireActivity().getSharedPreferences(SECURE_ARCHIVE, Context.MODE_PRIVATE)
            .edit().apply {
                putBoolean(LOCK_ARCHIVE, shouldLock)
                apply()
            }
    }
}