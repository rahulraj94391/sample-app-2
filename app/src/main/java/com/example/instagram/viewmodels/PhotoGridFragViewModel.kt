package com.example.instagram.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.OnePhotoPerPost
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class PhotoGridFragViewModel(app: Application): AndroidViewModel(app) {
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    val usersPost = MutableLiveData<MutableList<OnePhotoPerPost>>()
    val usersTaggedPost = MutableLiveData<MutableList<OnePhotoPerPost>>()
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun getProfilePost(profileId: Long) {
        val postIds = viewModelScope.async {
            db.postDao().getAllPostOfProfile(profileId)
        }
        val urlsOfOnePhotoPerPost = getOneImagePerPost(postIds.await())
        usersPost.postValue(urlsOfOnePhotoPerPost)
    }

    private suspend fun getOneImagePerPost(postIds: MutableList<Long>): MutableList<OnePhotoPerPost> {
        val imgURLList = mutableListOf<OnePhotoPerPost>()

        for (postId in postIds) {
            val snapShots = firebaseFireStore
                .collection("postImages")
                .whereEqualTo("serial", "${postId}_0")
                .get()
                .await()

            for (i in snapShots) {
                val link = i.data["$postId"].toString()
                imgURLList.add(OnePhotoPerPost(postId, link))
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