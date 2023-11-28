package com.example.instagram.fragments

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
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
import com.example.instagram.HomeActivity
import com.example.instagram.ImageUtil
import com.example.instagram.MSharedPreferences
import com.example.instagram.MainActivity
import com.example.instagram.MainViewModel
import com.example.instagram.PasswordHashing
import com.example.instagram.R
import com.example.instagram.database.AppDatabase
import com.example.instagram.databinding.FragmentDeleteAccountBinding
import com.example.instagram.dialog.SaveImagesDialog
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private const val TAG = "DeleteAccountFragment_CommTag"

class DeleteAccountFragment : Fragment() {
    private lateinit var binding: FragmentDeleteAccountBinding
    private lateinit var db: AppDatabase
    private lateinit var mainViewModel: MainViewModel
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_delete_account, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTermAndConditionTextView()
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // set username in the username field
        lifecycleScope.launch {
            val username = db.loginCredDao().getUsername(mainViewModel.loggedInProfileId!!)
            withContext(Dispatchers.Main) {
                binding.firstUsernameField.setText(username)
            }
        }
        
        binding.deleteButton.setOnClickListener {
            if (binding.passwordField.text?.isBlank() == true) {
                binding.textInputPassword.error = "Enter password"
                return@setOnClickListener
            }
            if (binding.checkbox1.checkedState != MaterialCheckBox.STATE_CHECKED) {
                Toast.makeText(requireContext(), "Accept the terms and condition to proceed.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val isCorrect = checkCredentials(binding.firstUsernameField.text.toString(), binding.passwordField.text.toString())
                if (isCorrect) {
                    showDeleteConfirmationDialog()
                }
            }
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account ?")
            .setMessage("Your account will be deleted permanently.\n\nYour posts images will be saved in internal storage.")
            .setCancelable(true)
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("No") { _, _ ->
            
            }
            .show()
    }
    
    private fun deleteAccount() {
        
        val fm = (requireActivity() as HomeActivity).navHostFragment.childFragmentManager
        val downloadDialog = SaveImagesDialog().show(fm, "save_post_images")
        
        
        lifecycleScope.launch {
            saveData(mainViewModel.loggedInProfileId!!)
            val profile = db.profileDao().getProfile(mainViewModel.loggedInProfileId!!)
            db.profileDao().deleteProfile(profile)
            val loginIntent = Intent(requireContext(), MainActivity::class.java)
            requireActivity().finish()
            changeSharedPreference()
            startActivity(loginIntent)
        }
    }
    
    private fun changeSharedPreference() {
        Log.d(TAG, "Change pref from Delete account")
        val sharedPref = requireActivity().getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        
        // remove login from preferences
        sharedPref.edit().apply {
            putLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
            putBoolean(MSharedPreferences.IS_LOGGED_IN, false)
            apply()
        }
        
        // set biometric to false in shared preferences
        val settingsPref = requireActivity().getSharedPreferences(SETTINGS_PREF_NAME, Context.MODE_PRIVATE)
        settingsPref.edit().putBoolean(BIOMETRIC_KEY, false).apply()
    }
    
    private suspend fun checkCredentials(username: String, password: String): Boolean {
        val db = AppDatabase.getDatabase(requireContext())
        val hashedPassword = PasswordHashing.generateSHA256Hash(password)!!
        val id: Long? = db.loginCredDao().loginWithCred(username, hashedPassword)
        binding.textInputPassword.error = null
        if (id == null) {
            withContext(Dispatchers.Main) {
                binding.textInputPassword.error = "Wrong password"
            }
            return false
        }
        return true
    }
    
    private fun setTermAndConditionTextView() {
        val spannableString = SpannableStringBuilder(binding.terms.text)
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(view: View) {
                showTermsAndCondition()
            }
        }, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        
        binding.terms.apply {
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
    
    private fun showTermsAndCondition() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Terms and conditions")
            .setMessage(
                """
                    Before you proceed to delete your account permanently, please note:

                    - This action is irreversible. All your data, including posts, messages, and personal information, will be permanently removed from our platform.
                    
                    - You will lose access to your account, and you will not be able to recover it.
                    
                    
                    By proceeding, you acknowledge and accept these terms.
                """.trimIndent()
            )
            .setCancelable(true)
            .setPositiveButton("Ok") { _, _ ->
            }.show()
    }
    
    private suspend fun getAllLinks(profileId: Long): List<String> {
        val imageUtil = ImageUtil(requireContext())
        val db = AppDatabase.getDatabase(requireContext())
        val allPostIds = db.postDao().getAllPostOfProfile(profileId)
        val allPhotoLinks = mutableListOf<String>()
        for (postId in allPostIds) {
            allPhotoLinks.addAll(imageUtil.getPostImages(postId))
        }
        imageUtil.getProfilePictureUrl(profileId)?.let { allPhotoLinks.add(it) }
        return allPhotoLinks
    }
    
    private suspend fun saveData(profileId: Long) {
        val imageUtil = ImageUtil(requireContext())
        val allImageUrl = getAllLinks(profileId)
        val dateFormat = SimpleDateFormat("dd-MM-yy HH-mm-ss", Locale("en", "IN"))
        val formattedDate = dateFormat.format(Date(System.currentTimeMillis()))
        val folderLabel = "Instagram $formattedDate"
        
        for (i in allImageUrl.indices) {
            val bitmap = imageUtil.getBitmap(allImageUrl[i])
            val cv = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "image$i.jpeg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + folderLabel)
            }
            val resolver = requireActivity().contentResolver
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
            val fos = imageUri?.let { resolver.openOutputStream(it) }
            fos?.let {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
            }
        }
    }
    
}