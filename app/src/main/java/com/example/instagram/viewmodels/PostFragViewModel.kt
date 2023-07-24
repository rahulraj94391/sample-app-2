package com.example.instagram.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.ImageUtil
import com.example.instagram.MSharedPreferences
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Post
import com.example.instagram.database.entity.PostText
import com.example.instagram.database.entity.Tag
import com.example.instagram.database.model.SearchResult
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "CommTag_PostFragmentViewModel"

class PostFragViewModel(private val app: Application) : AndroidViewModel(app) {
    var postImagesUri: MutableList<Uri> = mutableListOf()
    var profileId: Long = -1
    private var storageRef: FirebaseStorage
    private var firebaseFirestore: FirebaseFirestore
    var tempListTagUser = MutableLiveData<MutableList<SearchResult>>()
    var finalTagUserIds = mutableListOf<Pair<Chip, Long>>()
    var tagsToUpload = mutableListOf<Long>()
    var finalTextToUpload = ""
    private val imageUtil = ImageUtil(app)

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
        val postText = viewModelScope.async { db.postTextDao().insertPostText(PostText(postId, finalTextToUpload)) }

        val downscaleImageUris = imageUtil.getUriDownscaleImages(postImagesUri)
//        Log.d(TAG, "URI_TEST - downscaled imag uris = $downscaleImageUris")
        uploadPostImages(postId, downscaleImageUris)
        if (tagsToUpload.size > 0) {
            db.tagPeopleDao().insertPostTags(prepareTagsOnPost(postId))
        }


        // clear all variables after inserting.
        postImagesUri = mutableListOf()
        finalTagUserIds.clear()
        tagsToUpload.clear()
    }

    private fun prepareTagsOnPost(postId: Long): MutableList<Tag> {
        val list = mutableListOf<Tag>()
        for (i in tagsToUpload) {
            list.add(Tag(postId, i))
        }
        return list
    }


    suspend fun getSearchResult(name: String) {
        val sharedPref = app.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val ownID = sharedPref.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
        val db = AppDatabase.getDatabase(app)

        val usersRes = viewModelScope.async {
            db.searchDao().getSearchResult(name, ownID)
        }
        tempListTagUser.postValue(usersRes.await())

    }

    suspend fun getNameOfUser(profileId: Long): String {
        val db = AppDatabase.getDatabase(app)
        val fullName = db.profileDao().getFullName(profileId)
        return "${fullName.first_name} ${fullName.last_name}"
    }

    private fun uploadPostImages(postId: Long, listOfImageUris: List<Uri>) {
        Log.d(TAG, "Uri Length = ${listOfImageUris.size}")
        var passCounter = 0
        var failCounter = 0
        for (i in listOfImageUris.indices) {
            val storageRef = storageRef.reference.child("${postId}_$i")
            val imageUri = listOfImageUris[i]
            imageUri.let { uri ->
                storageRef.putFile(uri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                            val map = HashMap<String, Any>()
                            map[postId.toString()] = uri2.toString()
                            map["serial"] = "${postId}_${i}"

                            firebaseFirestore.collection("postImages").add(map).addOnCompleteListener { firestoreTask ->
                                if (firestoreTask.isSuccessful) {
                                    passCounter++
                                }
                                else {
                                    failCounter++
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

        val message = if (failCounter > 0) {
            "$passCounter/$failCounter uploaded successfully"
        }
        else {
            "Photos uploaded successfully"
        }

        Toast.makeText(app, message, Toast.LENGTH_SHORT).show()
    }

}