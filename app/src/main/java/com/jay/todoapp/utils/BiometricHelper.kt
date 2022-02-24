package com.jay.todoapp.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper private constructor(private val activity: FragmentActivity){
    
    companion object {
        private var INSTANCE : BiometricHelper? = null
        fun getInstance(activity: FragmentActivity) : BiometricHelper {
            return INSTANCE ?: run {
                INSTANCE = BiometricHelper(activity)
                INSTANCE!!
            }
        }
    }
    
    fun isBioAuthAvailable() : Boolean{
        return (BiometricManager.from(activity)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            == BiometricManager.BIOMETRIC_SUCCESS ||
            BiometricManager.from(activity)
                .canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            == BiometricManager.BIOMETRIC_SUCCESS)
    }

    fun bio(isAuthenticated: (Boolean) -> Unit) {
        if (!isBioAuthAvailable()) {
            isAuthenticated(false)
            return
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("AndroidX Biometric")
            .setSubtitle("Authenticate user via Biometric")
            .setDescription("Please authenticate yourself here")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK
                    or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .setConfirmationRequired(true)
            .build()

        val authCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                isAuthenticated(true)
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                isAuthenticated(false)
            }
            override fun onAuthenticationFailed() {
                isAuthenticated(false)
            }
        }
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), authCallback)
            .authenticate(promptInfo)
    }
}