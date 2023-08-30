package com.example.instagram.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.instagram.HomeActivity
import com.example.instagram.MSharedPreferences.IS_LOGGED_IN
import com.example.instagram.MSharedPreferences.LOGGED_IN_PROFILE_ID
import com.example.instagram.MSharedPreferences.SHARED_PREF_NAME
import com.example.instagram.R
import com.example.instagram.database.AppDatabase
import com.example.instagram.databinding.FragmentLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "CommTag_LoginFragment"

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.createNewAccountBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_createNewAccountScreenOneFragment)
        }
        binding.loginBtn.setOnClickListener { performLogin() }
        binding.passwordField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) performLogin()
            true
        }
    }
    
    private fun performLogin() {
        val username = binding.usernameField.text.toString().trim()
        val password = binding.passwordField.text.toString()
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(requireContext(), "Enter details correctly.", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Details not entered correctly.")
            return
        }
        lifecycleScope.launch {
            binding.loginBtn.isEnabled = false
            binding.loginProgressIndicator.visibility = View.VISIBLE
            checkCredentials(username, password)
        }
    }
    
    private suspend fun checkCredentials(username: String, password: String) {
        val db = AppDatabase.getDatabase(requireContext())
        val id: Long? = db.loginCredDao().loginWithCred(username, password)
        //        Log.d(TAG, "ID after login with cred: $id")
        if (id == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show()
            }
            binding.loginProgressIndicator.visibility = View.INVISIBLE
            binding.loginBtn.isEnabled = true
            return
        }
        val sharedPref = requireActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putLong(LOGGED_IN_PROFILE_ID, id)
            putBoolean(IS_LOGGED_IN, true)
            apply()
        }
        
        withContext(Dispatchers.Main) {
            startActivity(Intent(requireContext(), HomeActivity::class.java))
            requireActivity().finish()
        }
    }
}