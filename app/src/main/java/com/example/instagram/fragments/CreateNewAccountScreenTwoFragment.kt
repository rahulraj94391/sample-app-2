package com.example.instagram.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.LoginCred
import com.example.instagram.databinding.FragmentCreateNewAccountScreenTwoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val PASS_LINE_1 = "Password must contains: "
const val PASS_LINE_2 = "atleast one uppercase and lowercase character."
const val PASS_LINE_3 = "eight characters."
const val PASS_LINE_4 = "atleast one special character."
const val PASS_LINE_5 = "atleast one number."
const val CHECK = "✓ "
const val CROSS = "╳ "

private const val TAG = "CommTag_CreateNewAccountScreenT"

class CreateNewAccountScreenTwoFragment : Fragment() {
    private lateinit var binding: FragmentCreateNewAccountScreenTwoBinding
    private lateinit var sharedViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_new_account_screen_two, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]


        binding.passwordField.addTextChangedListener(CustomTextWatcher())

        binding.signUpBtn.setOnClickListener {
            val username = binding.usernameField.text.toString()
            val password = binding.passwordField.text.toString()
            lifecycleScope.launch {
                abc(username, password)
            }
        }
    }

    private suspend fun abc(username: String, password: String) {
        val db = AppDatabase.getDatabase(requireContext())
        val isUsernameUniqueResponse = db.loginCredDao().isUsernameUnique(username)
        if ((isUsernameUniqueResponse != null) && (isUsernameUniqueResponse > 0)) { // run this block only when entered username is not unique in db.
            withContext(Dispatchers.Main) {
                binding.textInputUsername.error = "Username already exist, try another one."
            }
            return
        }
        else if (!checkPassword(password)) { // run this block when username is unique and password DOESN'T qualify the condition.

        }
        else { // run this block when username is unique and password qualifies the condition.
            sharedViewModel.newProfileSignup!!.apply {
                bio = binding.bio.text.toString()
            }
            val profileId = db.profileDao().insertNewProfile(sharedViewModel.newProfileSignup!!)
            val rowId: Long = db.loginCredDao().insertNewLoginCred(LoginCred(profileId, username, password))
            Log.d(TAG, "Id = $profileId, username_ID = $rowId")
            findNavController().navigate(R.id.action_createNewAccountScreenTwoFragment_to_loginFragment)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Account created.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPassword(pass: String): Boolean {
        return if (!checkUppercaseAndLowercase(pass) or !hasMinLength(pass) or !containsSpecialCharacter(pass) or !containsNumericCharacter(pass)) {
            val guide = buildPasswordGuide(pass)
            binding.passwordGuide.text = guide
            false
        }
        else {
            binding.passwordGuide.text = null
            true
        }
    }

    private fun checkUppercaseAndLowercase(password: String): Boolean {
        val regexPattern = "(?=.*[a-z])(?=.*[A-Z]).*"
        return regexPattern.toRegex().matches(password)
    }

    private fun hasMinLength(password: String): Boolean {
        val regexPattern = ".{8,}"
        return regexPattern.toRegex().matches(password)
    }

    private fun containsSpecialCharacter(password: String): Boolean {
        val regexPattern = ".*[!@#\$%^&*()\\-=_+\\[\\]{};':\"\\\\|,.<>/?].*"
        return regexPattern.toRegex().matches(password)
    }

    private fun containsNumericCharacter(password: String): Boolean {
        val regexPattern = ".*\\d.*"
        return regexPattern.toRegex().matches(password)
    }

    private fun buildPasswordGuide(password: String): String {
        val tempCheck: StringBuilder = java.lang.StringBuilder()
        tempCheck.append(PASS_LINE_1).append("\n")
        if (checkUppercaseAndLowercase(password)) tempCheck.append(CHECK).append(PASS_LINE_2).append("\n")
        else tempCheck.append(CROSS).append(PASS_LINE_2).append("\n")

        if (hasMinLength(password)) tempCheck.append(CHECK).append(PASS_LINE_3).append("\n")
        else tempCheck.append(CROSS).append(PASS_LINE_3).append("\n")

        if (containsSpecialCharacter(password)) tempCheck.append(CHECK).append(PASS_LINE_4).append("\n")
        else tempCheck.append(CROSS).append(PASS_LINE_4).append("\n")

        if (containsNumericCharacter(password)) tempCheck.append(CHECK).append(PASS_LINE_5).append("\n")
        else tempCheck.append(CROSS).append(PASS_LINE_5).append("\n")

        return tempCheck.toString()
    }

    inner class CustomTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            checkPassword(s.toString())
        }
    }
}