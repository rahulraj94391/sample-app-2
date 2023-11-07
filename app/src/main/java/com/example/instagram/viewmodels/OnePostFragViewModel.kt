package com.example.instagram.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Location
import com.example.instagram.database.model.OnePost

private const val TAG = "CommTag_OnePostFragViewModel"

class OnePostFragViewModel(app: Application) : AndroidViewModel(app) {
    private val imageUtil = ImageUtil(app)
    private val db = AppDatabase.getDatabase(app)
    var profileImageUrl = MutableLiveData<String>()
    val postImagesUrl = MutableLiveData<MutableList<String>>()
    val likeCount = MutableLiveData<Int>(0)
    
    suspend fun getPostDetails(postId: Long, currentProfileId: Long): OnePost {
        val liked = db.likesDao().isPostLikedByProfile(postId, currentProfileId)
        val saved = db.savedPostDao().isPostSavedByProfile(currentProfileId, postId)
        val profileName = db.postDao().getUsername(postId)
        val postTime = db.postDao().getPostTime(postId)
        val postText = db.postTextDao().getPostText(postId)
        val location = getLocation(postId)
        var likeStat = false
        if (liked > 0) likeStat = true
        var saveStat = false
        if (saved > 0) saveStat = true
        
        return OnePost(
            likeStat,
            saveStat,
            profileName,
            postText,
            postTime,
            location
        )
    }
    
    private suspend fun getLocation(postId: Long): Location? {
        val locationId = db.postDao().getLocationId(postId)
        return locationId?.let { db.locationDao().getLocation(it) }
    }
    
    suspend fun deletePost(postId: Long) = db.postDao().deletePost(postId)
    suspend fun getPostImages(postId: Long) {
        // val urls = imageUtil.getPostImages(postId)
        val urls = db.cacheDao().getCachedPostImages(postId)
        Log.d(TAG, "for post id $postId\nimage urls are ---\n$urls")
        postImagesUrl.postValue(urls)
    }
    
    suspend fun getLikeCount(postId: Long) = likeCount.postValue(db.likesDao().likeCount(postId))
    suspend fun getProfilePictureByPostId(postId: Long) {
        val profileId = db.postDao().getProfileId(postId)
        val profilePicture = db.cacheDao().getCachedProfileImage(profileId) ?: imageUtil.getProfilePictureUrl(profileId) ?: ""
        profileImageUrl.postValue(profilePicture)
    }
}