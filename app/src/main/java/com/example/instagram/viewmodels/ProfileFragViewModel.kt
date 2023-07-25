package com.example.instagram.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.ProfileSummary
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

private const val TAG = "CommTag_ProfileViewModel"

class ProfileFragViewModel(private val app: Application) : AndroidViewModel(app) {
    private var storageRef: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val db: AppDatabase = AppDatabase.getDatabase(app)
//    val profilePicToUpload = MutableLiveData<String>()

    suspend fun getProfileSummary(profileId: Long): ProfileSummary {
        val profilePic = viewModelScope.async { getProfilePicture(profileId) }
        val fullNameBio = viewModelScope.async { db.profileDao().getFullNameBio(profileId) }
        val postCount = viewModelScope.async { db.postDao().getPostCount(profileId) }
        val followerCount = viewModelScope.async { db.followDao().getFollowerCount(profileId) }
        val followingCount = viewModelScope.async { db.followDao().getFollowingCount(profileId) }
        val username = viewModelScope.async { db.loginCredDao().getUsername(profileId) }

        return ProfileSummary(
            profilePic.await(),
            fullNameBio.await().first_name,
            fullNameBio.await().last_name,
            fullNameBio.await().bio,
            postCount.await(),
            followerCount.await(),
            followingCount.await(),
            username.await()
        )
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

    fun uploadProfileImage(profileId: Long, profilePicUri: Uri?) {
        profilePicUri?.let { uri ->
            val storageRef = storageRef.reference.child("$profileId")
            storageRef.putFile(uri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                        val map = HashMap<String, Any>()
                        map[profileId.toString()] = uri2.toString()
                        map["ppid"] = profileId.toString()
                        firebaseFireStore.collection("profileImages").add(map).addOnCompleteListener { firestoreTask ->
//                            if (firestoreTask.isSuccessful) {
//                                Toast.makeText(app, "Uploaded successfully", Toast.LENGTH_SHORT).show()
//                            }
//                            else {
//                                Toast.makeText(app, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
//                            }

                            // attach placeholder image when unsuccessful while adding path to firestore(DB)
//                            binding.imageView.setImageResource(R.drawable.ic_launcher_background)
//                            binding.progressBar.visibility = View.INVISIBLE

                        }
                    }
                }
                else {
                    // when image upload to storage(drive like) fails
//                    Toast.makeText(app, task.exception?.message, Toast.LENGTH_SHORT).show()
//                    binding.imageView.setImageResource(R.drawable.ic_launcher_background)
//                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }
}