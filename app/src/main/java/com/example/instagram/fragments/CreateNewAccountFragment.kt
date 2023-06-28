package com.example.instagram.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.databinding.FragmentCreateNewAccountBinding

private const val TAG = "CommTag_CreateNewAccountFragment"

class CreateNewAccountFragment : Fragment() {
    private lateinit var binding: FragmentCreateNewAccountBinding
    private lateinit var sharedViewModel: MainViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_new_account, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding.firstNameField.addTextChangedListener(CustomTextWatcher(binding.firstNameField))
        binding.lastNameField.addTextChangedListener(CustomTextWatcher(binding.lastNameField))
        binding.phoneNumberField.addTextChangedListener(CustomTextWatcher(binding.phoneNumberField))
        binding.emailField.addTextChangedListener(CustomTextWatcher(binding.emailField))

        val gender = resources.getStringArray(R.array.gender)
        val genderArrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, gender)
        binding.genderField.setAdapter(genderArrayAdapter)


        binding.genderField.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) closeKeyboard(v)
        }

        binding.dobField.showSoftInputOnFocus = false
        binding.dobField.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                setDOB(v as EditText)
            }
        }

    }

    private fun setDOB(view: EditText) {

    }


    private fun closeKeyboard(view: View) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    inner class CustomTextWatcher(private val inputField: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            when (inputField.id) {
                R.id.firstNameField -> validateFirstName()
                R.id.lastNameField -> validateLastName()
                /*R.id.genderField -> validateGender()*/
                R.id.phoneNumberField -> validatePhoneNumber()
                R.id.emailField -> validateEmailId()
                /*R.id.dobField -> validateDOB()*/
            }
        }
    }


    private fun validateEmailId(): Boolean {
        val email = binding.textInputEmail.editText!!.text.toString().trim()
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return if (emailRegex.matches(email)) {
            binding.textInputEmail.error = null
            true
        }
        else if (email.isEmpty()) {
            binding.textInputEmail.error = "Field can't be empty."
            false
        }
        else {
            binding.textInputEmail.error = "Invalid email"
            false
        }
    }

    private fun validatePhoneNumber(): Boolean {
        val mob: String = binding.textInputPhoneNumber.editText!!.text.toString().trim()
        return if (mob.isEmpty()) {
            binding.textInputPhoneNumber.error = "Field can't be empty."
            false
        }
        else if (mob.length != 10) {
            binding.textInputPhoneNumber.error = "Must contains 10 digits"
            false
        }
        else {
            binding.textInputPhoneNumber.error = null
            true
        }
    }

    private fun validateLastName(): Boolean {
        val name: String = binding.textInputLastName.editText!!.text.toString().trim()
        if (name.isEmpty()) {
            binding.textInputLastName.error = "Field can't be empty."
            return false
        }
        if (containsSpecialCharacter(name)) {
            binding.textInputLastName.error = "Only alphabets are allowed."
            return false
        }
        binding.textInputLastName.error = null
        return true
    }

    private fun validateFirstName(): Boolean {
        val name: String = binding.textInputFirstName.editText!!.text.toString().trim()
        if (name.isEmpty()) {
            binding.textInputFirstName.error = "Field can't be empty."
            return false
        }
        if (containsSpecialCharacter(name)) {
            binding.textInputFirstName.error = "Only alphabets are allowed."
            return false
        }
        binding.textInputFirstName.error = null
        return true
    }

    private fun containsSpecialCharacter(name: String): Boolean {
        name.toCharArray().forEach {
            if (it !in 'a'..'z' && it !in 'A'..'Z' && it != ' ') {
                return true
            }
        }
        return false
    }
}