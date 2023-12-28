package com.example.instagram.screen_newSignup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.instagram.CHECK
import com.example.instagram.CROSS
import com.example.instagram.HomeActivity
import com.example.instagram.PASS_LINE_1
import com.example.instagram.PASS_LINE_2
import com.example.instagram.PASS_LINE_3
import com.example.instagram.PASS_LINE_4
import com.example.instagram.PASS_LINE_5
import com.example.instagram.R
import com.example.instagram.common.MainViewModel
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.common.util.MSharedPreferences
import com.example.instagram.common.util.PasswordHashing
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.entity.LoginCred
import com.example.instagram.databinding.FragmentCreateNewAccountScreenTwoBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates


class SignupScreenTwoFragment : Fragment() {
    private var _binding: FragmentCreateNewAccountScreenTwoBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedViewModel: MainViewModel
    private var storageRef: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var imageUtil: ImageUtil
    private var profileId: Long by Delegates.notNull()
    private var isAccountCreationDone = MutableLiveData(false)
    private lateinit var profilePicUriToUpload: Uri
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_new_account_screen_two, container, false)
        imageUtil = ImageUtil(requireContext())
        return binding.root
    }
    
    
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding.uploadNewPicture.setOnClickListener {
            profilePicUri.launch("image/*")
        }
        
        isAccountCreationDone.observe(viewLifecycleOwner) {
            if (it == true) {
                lifecycleScope.launch {
                    login()
                }
            }
        }
        binding.passwordField.addTextChangedListener(CustomTextWatcher())
        
        binding.signUpBtn.setOnClickListener {
            val username = binding.usernameField.text.toString()
            val password = binding.passwordField.text.toString()
            lifecycleScope.launch {
                createAccount(username, password)
            }
        }
    }
    
    private val profilePicUri = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            profilePicUriToUpload = it
            binding.profileImage.setImageURI(it)
        }
    }
    
    private suspend fun createAccount(username: String, password: String) {
        if (username.isBlank()) {
            Toast.makeText(requireContext(), "Username cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        
        val db = AppDatabase.getDatabase(requireContext())
        val isUsernameUniqueResponse = db.loginCredDao().isUsernameUnique(username)
        if ((isUsernameUniqueResponse != null) && (isUsernameUniqueResponse > 0)) { // run this block only when entered username is not unique in db.
            withContext(Dispatchers.Main) {
                binding.textInputUsername.error = "Username already exist, try another one."
            }
            return
        } else if (!checkPassword(password)) { // run this block when username is unique and password DOESN'T qualify the condition.
        
        } else { // run this block when username is unique and password qualifies the condition.
            sharedViewModel.newProfileSignup!!.apply {
                profile_id = System.currentTimeMillis()
                val bioText = binding.bio.text.toString()
                bio = bioText.trim().ifBlank {
                    "\uD83C\uDF0E Hello World.\n" +
                            "\uD83D\uDC68\u200D\uD83D\uDCBC Working at Zoho."
                }
            }
            val passwordHash = PasswordHashing.generateSHA256Hash(password)
            if (passwordHash == null) {
                Toast.makeText(requireContext(), "Error occurred while hashing password.\nTry again.", Toast.LENGTH_SHORT).show()
                return
            }
            profileId = db.profileDao().insertNewProfile(sharedViewModel.newProfileSignup!!)
            db.loginCredDao().insertNewLoginCred(LoginCred(profileId, username, passwordHash))
            if (::profilePicUriToUpload.isInitialized) uploadProfileImage()
            else {
                binding.signUpBtn.isEnabled = false
                login()
            }
            
        }
    }
    
    private suspend fun login() {
        val sharedPref = requireActivity().getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, profileId)
            putBoolean(MSharedPreferences.IS_LOGGED_IN, true)
            apply()
        }
        
        withContext(Dispatchers.Main) {
            startActivity(Intent(requireContext(), HomeActivity::class.java))
            requireActivity().finish()
        }
    }
    
    private fun checkPassword(pass: String): Boolean {
        return if (!checkUppercaseAndLowercase(pass) or !hasMinLength(pass) or !containsSpecialCharacter(pass) or !containsNumericCharacter(pass)) {
            val guide = buildPasswordGuide(pass)
            binding.passwordGuide.text = guide
            false
        } else {
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
    
    private suspend fun getProfilePicture(profileId: Long): String? {
        var profileImageUrl: String? = null
        val snapShot = firebaseFireStore
            .collection("profileImages")
            .whereEqualTo("ppid", "$profileId")
            .get()
            .await()
        for (i in snapShot) {
            profileImageUrl = i.data["$profileId"].toString()
            break
        }
        return profileImageUrl
    }
    
    
    override fun onDestroy() {
        imageUtil.clearTempFiles()
        super.onDestroy()
    }
    
    private suspend fun uploadProfileImage() {
        profilePicUriToUpload.let { urii ->
            val uri = imageUtil.getUriDownscaleImages(mutableListOf(urii))
            binding.profileImage.alpha = 0.3F
            binding.indicator.visibility = View.VISIBLE
            binding.uploadNewPicture.isEnabled = false
            binding.signUpBtn.isEnabled = false
            val storageRef = storageRef.reference.child("$profileId")
            storageRef.putFile(uri[0]).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                        val map = HashMap<String, Any>()
                        map[profileId.toString()] = uri2.toString()
                        map["ppid"] = profileId.toString()
                        firebaseFireStore.collection("profileImages").add(map).addOnCompleteListener { firestoreTask ->
                            if (firestoreTask.isSuccessful) {
                                lifecycleScope.launch {
                                    val profilePicUrl = getProfilePicture(profileId) ?: return@launch
                                    withContext(Dispatchers.Main) {
                                        binding.profileImage.setImageBitmap(imageUtil.getBitmap(profilePicUrl))
                                        binding.profileImage.alpha = 1F
                                        binding.indicator.visibility = View.GONE
                                        binding.uploadNewPicture.isEnabled = true
                                        isAccountCreationDone.postValue(true)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}