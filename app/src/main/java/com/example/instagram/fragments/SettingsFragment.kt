package com.example.instagram.fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.instagram.R
import com.example.instagram.databinding.FragmentSettingsBinding

private const val TAG = "SettingsFragment_CommTag"

const val SETTINGS_PREF_NAME = "settings_pref"

const val DARK_MODE = 11
const val LIGHT_MODE = 22
const val AUTO = 33
const val THEME_KEY = "theme_key"

const val BIOMETRIC_KEY = "biometric"

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sharedPref: SharedPreferences
    
    private val biometricListener = CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
        Log.d(TAG, "Button status is: $isChecked")
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = requireActivity().getSharedPreferences(SETTINGS_PREF_NAME, MODE_PRIVATE)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.radioGroup.setOnCheckedChangeListener(themeListener)
        binding.biometricSwitch.setOnCheckedChangeListener(biometricListener)
        
        when (sharedPref.getInt(THEME_KEY, AUTO)) {
            AUTO -> binding.rbDeviceSettings.isChecked = true
            LIGHT_MODE -> binding.rbLightMode.isChecked = true
            DARK_MODE -> binding.rbDarkMode.isChecked = true
        }
        binding.biometricSwitch.isChecked = sharedPref.getBoolean(BIOMETRIC_KEY, false)
    }
}