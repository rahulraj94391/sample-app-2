package com.example.instagram.screen_singlePostView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.entity.Location
import com.example.instagram.screen_singlePostView.model.Post

class PostViewModel(app: Application) : AndroidViewModel(app) {
    private val imageUtil = ImageUtil(app)
    private val db = AppDatabase.getDatabase(app)
    var profileImageUrl = MutableLiveData<String>()
    val postImagesUrl = MutableLiveData<MutableList<String>>()
    val likeCount = MutableLiveData<Int>(0)
    
    suspend fun getPostDetails(postId: Long, currentProfileId: Long): Post {
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
        
        return Post(
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
        val urls = db.cacheDao().getCachedPostImages(postId)
        postImagesUrl.postValue(urls)
    }
    
    suspend fun getLikeCount(postId: Long) = likeCount.postValue(db.likesDao().likeCount(postId))
    
    suspend fun getProfilePictureByPostId(postId: Long) {
        val profileId = db.postDao().getProfileId(postId)
        val profilePicture = db.cacheDao().getCachedProfileImage(profileId) ?: imageUtil.getProfilePictureUrl(profileId) ?: ""
        profileImageUrl.postValue(profilePicture)
    }
}