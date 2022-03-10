package com.jay.todoapp.fragments.signIn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.jay.todoapp.R
import com.jay.todoapp.data.viewmodel.ToDoSharedViewModel
import com.jay.todoapp.databinding.FragmentSignInBinding
import com.jay.todoapp.utils.LOG_TAG

class SignInFragment : Fragment() {

    private var _binding : FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    private val sharedViewModel : ToDoSharedViewModel by activityViewModels()

    companion object {
        // This can be any number you want
        private const val RC_SIGN_IN = 120
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        // Inflate the layout for this fragment
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("785704444017-dq820spfbsll8ji62p9hrg66tumuqouv.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // auth instance
        mAuth = FirebaseAuth.getInstance()

        binding.signInButton.setOnClickListener {
            googleSignInClient.signOut()
            signIn()
        }
//        binding.noSignInButton.setOnClickListener {
//            onNoSignInOptionClicked()
//            findNavController().navigate(R.id.action_signInFragment_to_listFragment)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
    * SIGN IN METHODS
    * */

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if(task.isSuccessful) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(LOG_TAG, "firebaseAuthWithGoogle:" + account.id)
//                    NetworkToDoRepository.getInstance().setCollection()
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(LOG_TAG, "Google sign in failed", e)
                }
            } else {
                Log.w(LOG_TAG, exception.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(LOG_TAG, "signInWithCredential:success")
                    sharedViewModel.getAllNotes()
                    findNavController().navigate(R.id.action_signInFragment_to_listFragment)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(LOG_TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

//    /*
//    * SHARED PREFERENCE FOR SIGN IN
//    * */
//    private fun onNoSignInOptionClicked() {
//        val sharedPref = requireActivity().getSharedPreferences("SignIn", Context.MODE_PRIVATE)
//        val editor = sharedPref.edit()
//        editor.putBoolean("SignIn", false)
//        editor.apply()
//    }
}