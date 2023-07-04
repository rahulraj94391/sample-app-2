package com.example.instagram.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.instagram.HomeActivity
import com.example.instagram.MSharedPreferences.IS_FIRST_TIME
import com.example.instagram.MSharedPreferences.IS_LOGGED_IN
import com.example.instagram.MSharedPreferences.SHARED_PREF_NAME
import com.example.instagram.R
import com.example.instagram.databinding.FragmentSplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CommTag_SplashFragment"

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private lateinit var sharedPref: SharedPreferences
    private var scope = lifecycleScope

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref = requireActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

        if (sharedPref.getBoolean(IS_FIRST_TIME, true)) {
            // do something for first time user
            Log.d(TAG, "First time opened.")


            sharedPref.edit().putBoolean(IS_FIRST_TIME, false).apply()
        }

        scope.launch {
            delay(2000)
            if (sharedPref.getBoolean(IS_LOGGED_IN, false)) {
                // launch activity for home screen when user is logged in.
                startActivity(Intent(requireContext(), HomeActivity::class.java))
                requireActivity().finish()
            }
            else {
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
        }
    }
}