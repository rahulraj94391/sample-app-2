package com.example.instagram.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.database.entity.Profile
import com.example.instagram.databinding.FragmentCreateNewAccountScreenOneBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

private const val TAG = "CreateNewAccountFragment_CommTag"

class CreateNewAccountScreenOneFragment : Fragment() {
    private lateinit var binding: FragmentCreateNewAccountScreenOneBinding
    private lateinit var sharedViewModel: MainViewModel
    
    private lateinit var firstName: TextInputLayout
    private lateinit var lastName: TextInputLayout
    private lateinit var phoneNumber: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var gender: TextInputLayout
    private lateinit var dob: TextInputLayout
    
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var autoTVGender: AutoCompleteTextView
    private lateinit var etDob: TextInputEditText
    
    private var dobDate: Calendar = Calendar.getInstance()
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_new_account_screen_one, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        
        val newBind = binding.includedProfileLayout
        firstName = newBind.findViewById(R.id.text_input_first_name)
        lastName = newBind.findViewById(R.id.text_input_last_name)
        phoneNumber = newBind.findViewById(R.id.text_input_phone_number)
        email = newBind.findViewById(R.id.text_input_email)
        gender = newBind.findViewById(R.id.text_input_gender)
        dob = newBind.findViewById(R.id.text_input_dob)
        
        etFirstName = newBind.findViewById(R.id.firstNameField)
        etLastName = newBind.findViewById(R.id.lastNameField)
        etPhoneNumber = newBind.findViewById(R.id.phoneNumberField)
        etEmail = newBind.findViewById(R.id.emailField)
        autoTVGender = newBind.findViewById(R.id.genderField)
        etDob = newBind.findViewById(R.id.dobField)
        
        // text watcher
        etFirstName.addTextChangedListener(CustomTextWatcher(etFirstName))
        etLastName.addTextChangedListener(CustomTextWatcher(etLastName))
        etPhoneNumber.addTextChangedListener(CustomTextWatcher(etPhoneNumber))
        etEmail.addTextChangedListener(CustomTextWatcher(etEmail))
        
        // adapter for gender field (autocomplete text view)
        val genderArr = resources.getStringArray(R.array.gender)
        val genderArrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, genderArr)
        autoTVGender.setAdapter(genderArrayAdapter)
        
        
        etDob.showSoftInputOnFocus = false
        etDob.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                setDOB()
            }
        }
        
        autoTVGender.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            gender.error = null
        }
        
        binding.nextBtn.setOnClickListener {
            if (!validateFirstName() or
                !validateLastName() or
                !validatePhoneNumber() or
                !validateEmailId() or
                !validateGender() or
                !validateDOB()
            ) {
                // some errors, don't proceed forward
            } else {
                // all ok, proceed forward
                sharedViewModel.newProfileSignup = Profile(
                    first_name = etFirstName.text.toString(),
                    last_name = etLastName.text.toString(),
                    dob = dobDate.timeInMillis,
                    phone_number = etPhoneNumber.text.toString(),
                    email_id = etEmail.text.toString(),
                    gender = autoTVGender.text.toString()
                )
                findNavController().navigate(R.id.action_createNewAccountScreenOneFragment_to_createNewAccountScreenTwoFragment)
            }
        }
    }
    
    private fun setDOB() {
        val c: Calendar = Calendar.getInstance()
        val currDay = c.get(Calendar.DAY_OF_MONTH)
        val currMonth = c.get(Calendar.MONTH)
        val currYear = c.get(Calendar.YEAR)
        
        val datePicker = DatePickerDialog(requireContext(), R.style.MaterialCalendarTheme, { _, year, month, dayOfMonth ->
            etDob.setText(resources.getString(R.string.date_of_birth_format, dayOfMonth, (month + 1), year))
            etDob.clearFocus()
            this.dob.error = null
            // dobDate = Calendar.getInstance()
            dobDate.set(year, month, dayOfMonth)
        }, currYear, currMonth, currDay)
        datePicker.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePicker.setOnDismissListener { etDob.clearFocus() }
        datePicker.show()
    }
    
    inner class CustomTextWatcher(private val inputField: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        
        }
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        
        }
        
        override fun afterTextChanged(s: Editable?) {
            
            Log.d(TAG, "insidewhen")
            when (inputField.id) {
                R.id.firstNameField -> {
                    if (requireActivity().findViewById<TextInputEditText>(R.id.firstNameField).hasFocus())
                        validateFirstName()
                }
                
                R.id.lastNameField -> {
                    if (requireActivity().findViewById<TextInputEditText>(R.id.lastNameField).hasFocus())
                        validateLastName()
                }
                
                R.id.genderField -> {
                    validateGender()
                }
                
                R.id.phoneNumberField -> {
                    if (requireActivity().findViewById<TextInputEditText>(R.id.phoneNumberField).hasFocus())
                        validatePhoneNumber()
                }
                
                R.id.emailField -> {
                    if (requireActivity().findViewById<TextInputEditText>(R.id.emailField).hasFocus())
                        validateEmailId()
                }
                
                R.id.dobField -> validateDOB()
            }
        }
    }
    
    private fun validateGender(): Boolean {
        val genderFieldText = autoTVGender.text.toString()
        return if (genderFieldText.isEmpty()) {
            gender.error = resources.getString(R.string.field_cant_be_empty)
            return false
        } else {
            gender.error = null
            return true
        }
    }
    
    private fun validateDOB(): Boolean {
        val dobFieldText = etDob.text.toString()
        return if (dobFieldText.isEmpty()) {
            dob.error = resources.getString(R.string.field_cant_be_empty)
            false
        } else {
            dob.error = null
            true
        }
    }
    
    
    private fun validateEmailId(): Boolean {
        val emailStr = etEmail.text.toString().trim()
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return if (emailRegex.matches(emailStr)) {
            email.error = null
            true
        } else if (emailStr.isEmpty()) {
            email.error = resources.getString(R.string.field_cant_be_empty)
            false
        } else {
            email.error = resources.getString(R.string.invalid_email)
            false
        }
    }
    
    private fun validatePhoneNumber(): Boolean {
        val mob: String = etPhoneNumber.text.toString().trim()
        return if (mob.isEmpty()) {
            phoneNumber.error = resources.getString(R.string.field_cant_be_empty)
            false
        } else if (mob.length != 10) {
            phoneNumber.error = resources.getString(R.string.must_contain_10_digit)
            false
        } else {
            phoneNumber.error = null
            true
        }
    }
    
    private fun validateLastName(): Boolean {
        val name: String = etLastName.text.toString().trim()
        if (name.isEmpty()) {
            lastName.error = resources.getString(R.string.field_cant_be_empty)
            return false
        }
        if (containsSpecialCharacter(name)) {
            lastName.error = resources.getString(R.string.only_alphabet_allowed)
            return false
        }
        lastName.error = null
        return true
    }
    
    private fun validateFirstName(): Boolean {
        val name: String = etFirstName.text.toString().trim()
        if (name.isEmpty()) {
            firstName.error = resources.getString(R.string.field_cant_be_empty)
            return false
        }
        if (containsSpecialCharacter(name)) {
            firstName.error = resources.getString(R.string.only_alphabet_allowed)
            return false
        }
        firstName.error = null
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