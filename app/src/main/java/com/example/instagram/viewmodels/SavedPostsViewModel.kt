package com.example.instagram.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.OnePhotoPerPost
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SavedPostsViewModel(app: Application) : AndroidViewModel(app) {
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val db = AppDatabase.getDatabase(app)
    val listOfSavedPosts = MutableLiveData<MutableList<OnePhotoPerPost>>()


    suspend fun getSavedPosts(profileId: Long) {
        val savedPostIds = db.savedPostDao().getAllSavedPosts(profileId)
        val oneImageAssociatedWithPostId = getOneImagePerPost(savedPostIds)
        listOfSavedPosts.postValue(oneImageAssociatedWithPostId)
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
}