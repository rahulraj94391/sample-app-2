package com.example.instagram.fragments

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.instagram.HomeActivity
import com.example.instagram.MSharedPreferences
import com.example.instagram.R
import com.example.instagram.databinding.FragmentSplashBinding

private const val TAG = "CommTag_SplashFragment"

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var settingsSharedPref: SharedPreferences
    private var scope = lifecycleScope
    private var cancellationSignall: CancellationSignal? = null
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "onAuthenticationError: errcode = $errorCode, errStr = $errString")
                //                binding.linearProgressBar.visibility = View.INVISIBLE
                binding.biometricMessage.text = errString
                binding.biometricMessage.visibility = View.VISIBLE
                binding.retryBiometric.visibility = View.VISIBLE
                Log.d(TAG, "Fingerprint operation cancelled by user")
            }
            
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                with(binding.biometricMessage) {
                    text = "Authentication successful"
                    setTextColor(resources.getColor(R.color.biometric_passed))
                    visibility = View.VISIBLE
                }
                binding.retryBiometric.visibility = View.INVISIBLE
                Toast.makeText(requireContext(), "Authentication Succeeded", Toast.LENGTH_SHORT).show()
                startHomeActivity()
            }
        }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = requireActivity().getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        settingsSharedPref = requireActivity().getSharedPreferences(SETTINGS_PREF_NAME, Context.MODE_PRIVATE)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.retryBiometric.setOnClickListener {
            showBiometricPrompt()
        }
    }
    
    override fun onResume() {
        super.onResume()
        val isBiometricEnabled = settingsSharedPref.getBoolean(BIOMETRIC_KEY, false)
        if (sharedPref.getBoolean(MSharedPreferences.IS_FIRST_TIME, true)) {
            sharedPref.edit().putBoolean(MSharedPreferences.IS_FIRST_TIME, false).apply()
        }
        
        if (sharedPref.getBoolean(MSharedPreferences.IS_LOGGED_IN, false)) {
            if (checkBiometricSupport() && isBiometricEnabled) showBiometricPrompt()
            else startHomeActivity()
        } else findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
    }
    
    private fun startHomeActivity() {
        startActivity(Intent(requireContext(), HomeActivity::class.java))
        requireActivity().finish()
    }
    
    private fun showBiometricPrompt() {
        val biometricPrompt = BiometricPrompt.Builder(requireActivity())
            .setTitle("Unlock Instagram")
            .setNegativeButton("cancel", requireActivity().mainExecutor) { dialog, which ->
                //                binding.linearProgressBar.visibility = View.INVISIBLE
                binding.biometricMessage.text = "Fingerprint operation cancelled by user"
                binding.biometricMessage.visibility = View.VISIBLE
                Log.d(TAG, "Fingerprint operation cancelled by user")
                binding.retryBiometric.visibility = View.VISIBLE
            }.build()
        biometricPrompt.authenticate(getCancellationSignal(), requireActivity().mainExecutor, authenticationCallback)
    }
    
    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val canAuthenticate = BiometricManager.from(requireContext()).canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL) == BIOMETRIC_SUCCESS
        val hasFeature = requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        
        if (!keyguardManager.isKeyguardSecure) {
            Log.d(TAG, "Fingerprint has not been enabled in settings.")
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Fingerprint authentication permission not enabled.")
        }
        return hasFeature && canAuthenticate
    }
    
    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignall = CancellationSignal()
        cancellationSignall?.setOnCancelListener {
            //            binding.linearProgressBar.visibility = View.INVISIBLE
            binding.biometricMessage.text = "Authentication cancelled"
            Log.d(TAG, "Authentication cancelled")
            binding.retryBiometric.visibility = View.VISIBLE
        }
        return cancellationSignall as CancellationSignal
    }
}