package com.example.instagram.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.OnePhotoPerPost
import kotlinx.coroutines.async

private const val TAG = "CommTag_PhotoGridFragViewModel"

class PhotoGridFragViewModel(app: Application) : AndroidViewModel(app) {
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    val usersPost = MutableLiveData<MutableList<OnePhotoPerPost>>()
    val imageUtil = ImageUtil(app)
    val usersTaggedPost = MutableLiveData<MutableList<OnePhotoPerPost>>()
    var isUserBlocked = false
        private set
    
    suspend fun getProfilePost(profileId: Long, myId: Long, itemCount: Int) {
        if (isUserBlocked(myId, profileId)) {
            usersPost.postValue(mutableListOf())
            return
        }
        
        val postIdsAsync = viewModelScope.async {
            db.postDao().getPostOfProfile(profileId = profileId, offset = itemCount)
        }
        val postIds = postIdsAsync.await()
        // val onePhotoPerPost = imageUtil.getOneImagePerPost(postIds.await())
        val onePhotoPerPost = mutableListOf<OnePhotoPerPost>()
        
        for (postId in postIds) {
            val photoLink = try {
                db.cacheDao().getFirstImgFromEachPost(postId) ?: imageUtil.getOneImagePerPost(mutableListOf(postId))[0].imageURl
            } catch (e: Exception) {
                ""
            }
            val onePhoto = OnePhotoPerPost(postId, photoLink)
            onePhotoPerPost.add(onePhoto)
        }
        
        Log.d(TAG, "onePhotoPerPost: $onePhotoPerPost")
        
        usersPost.postValue(onePhotoPerPost)
    }
    
    suspend fun getAllPostWhereProfileIsTagged(profileId: Long, myId: Long, itemCount: Int) {
        if (isUserBlocked(myId, profileId)) {
            usersTaggedPost.postValue(mutableListOf())
            return
        }
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
        usersTaggedPost.postValue(onePhotoPerPost)
    }
    
    private suspend fun isUserBlocked(ownProfileId: Long, userProfileId: Long): Boolean {
        val count = db.blockDao().isBlocked(ownProfileId, userProfileId)
        val isBlocked = if (count > 0) {
            Log.d(TAG, "user is blocked")
            true
        } else {
            Log.d(TAG, "user is NOT blocked")
            false
        }
        isUserBlocked = isBlocked
        return isBlocked
    }
}