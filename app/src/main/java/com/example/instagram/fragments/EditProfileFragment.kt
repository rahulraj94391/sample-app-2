package com.example.instagram.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.findNavController
import com.example.instagram.ImageUtil
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.database.AppDatabase
import com.example.instagram.databinding.FragmentEditProfileBinding
import com.example.instagram.viewmodels.ProfileFragViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val TAG = "CommTag_EditProfileFragment"

class EditProfileFragment : Fragment() {
    private val isUploadComplete = MutableLiveData(false)
    private var storageRef: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var mainViewModel: MainViewModel
    private lateinit var imageUtil: ImageUtil
    private lateinit var viewModel: ProfileFragViewModel
    private lateinit var docId: String
    private lateinit var imageUriToUpload: Uri
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        imageUtil = ImageUtil(requireContext())
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel = ViewModelProvider(requireActivity())[ProfileFragViewModel::class.java]
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    
    private val profilePicUri = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            binding.profileImage.setImageURI(it)
            imageUriToUpload = it
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch { loadData() }
        val uploadNewPic = binding.uploadNewPicture
        uploadNewPic.setOnClickListener {
            profilePicUri.launch("image/*")
        }
        binding.saveChanges.setOnClickListener {
            it.isEnabled = false
            
            val fNameIsBlank = binding.firstNameField.text.toString().isBlank()
            val lNameIsBlank = binding.lastNameField.text.toString().isBlank()
            
            if (!fNameIsBlank && !lNameIsBlank) {
                lifecycleScope.launch { saveData() }
            } else {
                it.isEnabled = true
                val msg = if (fNameIsBlank) {
                    "First name cannot be blank"
                } else {
                    "Last name cannot be blank"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
        
        isUploadComplete.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
            }
        }
    }
    
    private suspend fun loadData() {
        lifecycleScope.launch {
            val profilePicUrl = getProfilePicture(mainViewModel.loggedInProfileId!!) ?: return@launch
            binding.profileImage.setImageBitmap(imageUtil.getBitmap(profilePicUrl))
        }
        
        val fullName = db.profileDao().getFullNameBio(mainViewModel.loggedInProfileId!!)
        binding.apply {
            firstNameField.setText(fullName.first_name)
            lastNameField.setText(fullName.last_name)
            bio.setText(fullName.bio)
        }
    }
    
    private suspend fun saveData() {
        db.profileDao().editProfile(
            binding.firstNameField.text.toString(),
            binding.lastNameField.text.toString(),
            binding.bio.text.toString().trim(),
            mainViewModel.loggedInProfileId!!
        )
        viewModel.getProfileSummary(mainViewModel.loggedInProfileId!!, mainViewModel.loggedInProfileId!!)
        //findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
        if (::imageUriToUpload.isInitialized) uploadProfileImage(mainViewModel.loggedInProfileId!!)
        else isUploadComplete.postValue(true)
        
    }
    
    private suspend fun getProfilePicture(profileId: Long): String? {
        val docId = mutableListOf<String>()
        val image = imageUtil.getProfilePictureUrl(profileId, docId)
        if (docId.size > 0) {
            this.docId = docId[0]
        }
        return image
    }
    
    private fun uploadProfileImage(profileId: Long) {
        mainViewModel.profileImageBitmap = null
        binding.profileImage.alpha = 0.3F
        binding.indicator.visibility = View.VISIBLE
        binding.uploadNewPicture.isEnabled = false
        imageUriToUpload.let { urii ->
            val uri = imageUtil.getUriDownscaleImages(mutableListOf(urii), 1.0, 480.0)
            
            val storageRef = storageRef.reference.child("$profileId")
            storageRef.putFile(uri[0]).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                        val map = HashMap<String, Any>()
                        map[profileId.toString()] = uri2.toString()
                        map["ppid"] = profileId.toString()
                        
                        if (::docId.isInitialized) {
                            firebaseFireStore.collection("profileImages").document(docId).set(map).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    lifecycleScope.launch {
                                        val profilePicUrl = getProfilePicture(mainViewModel.loggedInProfileId!!) ?: return@launch
                                        Log.d(TAG, "prof pic url = $profilePicUrl")
                                        withContext(Dispatchers.Main) {
                                            binding.profileImage.setImageBitmap(imageUtil.getBitmap(profilePicUrl))
                                            binding.profileImage.alpha = 1F
                                            binding.indicator.visibility = View.GONE
                                            binding.uploadNewPicture.isEnabled = true
                                            isUploadComplete.postValue(true)
                                            
                                        }
                                    }
                                }
                            }
                        } else {
                            firebaseFireStore.collection("profileImages").add(map).addOnCompleteListener { firestoreTask ->
                                if (firestoreTask.isSuccessful) {
                                    lifecycleScope.launch {
                                        val profilePicUrl = getProfilePicture(mainViewModel.loggedInProfileId!!) ?: return@launch
                                        Log.d(TAG, "prof pic url = $profilePicUrl")
                                        withContext(Dispatchers.Main) {
                                            binding.profileImage.setImageBitmap(imageUtil.getBitmap(profilePicUrl))
                                            binding.profileImage.alpha = 1F
                                            binding.indicator.visibility = View.GONE
                                            binding.uploadNewPicture.isEnabled = true
                                            isUploadComplete.postValue(true)
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
}
