package com.example.instagram.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.OnePhotoPerPost
import kotlinx.coroutines.async

private const val TAG = "CommTag_PhotoGridFragViewModel"

class PhotoGridFragViewModel(app: Application) : AndroidViewModel(app) {
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    val usersPost = MutableLiveData<MutableList<OnePhotoPerPost>>()
    val usersTaggedPost = MutableLiveData<MutableList<OnePhotoPerPost>>()
    
    suspend fun getProfilePost(profileId: Long, itemCount: Int) {
        Log.e(TAG, "getProfilePost: called in photo-grid-viewmodel")
        val postIdsAsync = viewModelScope.async {
            db.postDao().getAllPostOfProfile(profileId = profileId, offset = itemCount)
        }
        val postIds = postIdsAsync.await()
        // val onePhotoPerPost = imageUtil.getOneImagePerPost(postIds.await())
        val onePhotoPerPost = mutableListOf<OnePhotoPerPost>()
        
        for (postId in postIds) {
            val photoLink = db.cacheDao().getFirstImgFromEachPost(postId) ?: ""
            val onePhoto = OnePhotoPerPost(postId, photoLink)
            onePhotoPerPost.add(onePhoto)
        }
        
        usersPost.postValue(onePhotoPerPost)
    }
    
    suspend fun getAllPostWhereProfileIsTagged(profileId: Long, itemCount: Int) {
        val postIdsAsync = viewModelScope.async {
            db.tagPeopleDao().getAllTaggedPostOfProfile(profileId = profileId, offset = itemCount)
        }
        val postIds = postIdsAsync.await()
        
        // val urlsOfOnePhotoPerPost = imageUtil.getOneImagePerPost(defList.await())
        val onePhotoPerPost = mutableListOf<OnePhotoPerPost>()
        for (postId in postIds) {
            val photoLink = db.cacheDao().getFirstImgFromEachPost(postId) ?: ""
            onePhotoPerPost.add(OnePhotoPerPost(postId, photoLink))
        }
        usersTaggedPost.postValue(onePhotoPerPost.asReversed())
    }
    
    override fun onCleared() {
        Log.e(TAG, "Photo Grid Frag Cleared")
        super.onCleared()
    }
    
}