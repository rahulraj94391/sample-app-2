package com.example.instagram.screen_settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.instagram.AUTO
import com.example.instagram.BIOMETRIC_KEY
import com.example.instagram.DARK_MODE
import com.example.instagram.LIGHT_MODE
import com.example.instagram.R
import com.example.instagram.SETTINGS_PREF_NAME
import com.example.instagram.THEME_KEY
import com.example.instagram.common.MainViewModel
import com.example.instagram.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mainViewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = requireActivity().getSharedPreferences(SETTINGS_PREF_NAME, Context.MODE_PRIVATE)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        super.onViewCreated(view, savedInstanceState)
        binding.radioGroup.setOnCheckedChangeListener(themeListener)
        binding.biometricSwitch.setOnCheckedChangeListener(biometricListener)
        when (sharedPref.getInt(THEME_KEY, AUTO)) {
            AUTO -> binding.rbDeviceSettings.isChecked = true
            LIGHT_MODE -> binding.rbLightMode.isChecked = true
            DARK_MODE -> binding.rbDarkMode.isChecked = true
        }
        binding.biometricSwitch.isChecked = sharedPref.getBoolean(BIOMETRIC_KEY, false)
        binding.biometricSwitch.isEnabled = checkSupport() // enable switch when device supports biometric
        
        binding.deleteAccount.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.settingsFragment) return@setOnClickListener
            val action = SettingsFragmentDirections.actionSettingsFragmentToDeleteAccountFragment()
            findNavController().navigate(action)
            
        }
    }
    
    private val biometricListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            sharedPref.edit().putBoolean(BIOMETRIC_KEY, true).apply()
        } else {
            sharedPref.edit().putBoolean(BIOMETRIC_KEY, false).apply()
        }
    }
    
    private val themeListener = RadioGroup.OnCheckedChangeListener { _, id ->
        when (id) {
            R.id.rbDeviceSettings -> setSystemTheme()
            R.id.rbLightMode -> setLightTheme()
            R.id.rbDarkMode -> setDarkTheme()
        }
    }
    
    private fun checkSupport(): Boolean {
        val biometricManager = BiometricManager.from(requireContext())
        val canAuthenticate = when (biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
            BIOMETRIC_SUCCESS, BIOMETRIC_ERROR_NONE_ENROLLED -> true
            else -> false
        }
        return canAuthenticate
    }
    
    private fun setSystemTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        sharedPref.edit().putInt(THEME_KEY, AUTO).apply()
    }
    
    private fun setDarkTheme() {
        sharedPref.edit().putInt(THEME_KEY, DARK_MODE).apply()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    
    private fun setLightTheme() {
        sharedPref.edit().putInt(THEME_KEY, LIGHT_MODE).apply()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}