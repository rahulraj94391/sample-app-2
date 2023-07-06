package com.example.instagram.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.MSharedPreferences
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Post
import com.example.instagram.database.entity.PostText
import com.example.instagram.database.entity.Tag
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

private const val TAG = "CommTag_PostFragmentViewModel"

class PostFragmentViewModel(private val app: Application) : AndroidViewModel(app) {
    var postImagesUri: MutableList<Uri> = mutableListOf()
    var postText: String = ""
    var profileId: Long = -1
    var tagProfile: MutableList<Tag> = mutableListOf()
    private var storageRef: FirebaseStorage
    private var firebaseFirestore: FirebaseFirestore


    init {
        val sharedPreferences = app.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        profileId = sharedPreferences.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
        storageRef = FirebaseStorage.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    fun insertPost() {
        viewModelScope.launch { uploadPost() }
    }

    private suspend fun uploadPost() {
        val timeStamp = System.currentTimeMillis()
        val db = AppDatabase.getDatabase(app)
        val postId = db.postDao().insertPost(Post(profileId, timeStamp))
        val postText = db.postTextDao().insertPostText(PostText(postId, postText))
        uploadPostImages(postId, postImagesUri)

        if (tagProfile.size > 0) {
            db.tagPeopleDao().insertPostTags(tagProfile)
        }


        this.postText = ""
        postImagesUri = mutableListOf()
        tagProfile = mutableListOf()

    }

    private fun uploadPostImages(postId: Long, listOfImageUris: List<Uri>) {
        Log.d(TAG, "Uri Length = ${listOfImageUris.size}")
        for (i in listOfImageUris.indices) {
            val storageRef = storageRef.reference.child("${postId}_$i")
            val imageUri = listOfImageUris[i]
            imageUri.let { uri ->
                storageRef.putFile(uri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                            val map = HashMap<String, Any>()
                            map[postId.toString()] = uri2.toString()

                            firebaseFirestore.collection("postImages").add(map).addOnCompleteListener { firestoreTask ->
                                if (firestoreTask.isSuccessful) {
                                    Toast.makeText(app, "Uploaded successfully", Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    Toast.makeText(app, "Uploaded failed", Toast.LENGTH_SHORT).show()
                                }

                                // attach placeholder image when unsuccessful while adding path to firestore(DB)
                                /*binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                                binding.progressBar.visibility = View.INVISIBLE*/

                            }
                        }
                    }
                    else {
                        // when image upload to storage(drive like) fails
                        /*Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                        binding.progressBar.visibility = View.INVISIBLE*/
                    }
                }
            }
        }
    }

}