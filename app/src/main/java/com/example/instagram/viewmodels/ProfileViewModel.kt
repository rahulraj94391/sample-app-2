package com.example.instagram.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.database.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

private const val TAG = "CommTag_ProfileViewModel"

class ProfileViewModel(private val app: Application) : AndroidViewModel(app) {
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    val usersPost = MutableLiveData<MutableList<String>>()
    val usersTaggedPost = MutableLiveData<MutableList<String>>()

    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun getProfilePost(profileId: Long) {
        val defList = viewModelScope.async {
            db.postDao().getAllPostOfProfile(profileId)
        }
        val urlsOfOnePhotoPerPost = getOneImagePerPost(defList.await())
        usersPost.postValue(urlsOfOnePhotoPerPost)
    }

    private suspend fun getOneImagePerPost(postIds: MutableList<Long>): MutableList<String> {
        val imgURLList = mutableListOf<String>()

        for (postId in postIds) {
            val snapShots = firebaseFireStore
                .collection("postImages")
                .whereEqualTo("serial", "${postId}_0")
                .get()
                .await()

            for (i in snapShots) {
                Log.d(TAG, "QueryDocumentSnapshot! size = $i")
                val link = i.data["$postId"].toString()
                imgURLList.add(link)
                Log.d(TAG, "link for post = $link")
            }

        }
        return imgURLList
    }

    suspend fun getAllPostInWhichProfileIsTagged(profileId: Long) {
        val defList = viewModelScope.async {
            db.tagPeopleDao().getAllTaggedPostOfProfile(profileId)
        }
        val urlsOfOnePhotoPerPost = getOneImagePerPost(defList.await())
        usersTaggedPost.postValue(urlsOfOnePhotoPerPost)
    }
}