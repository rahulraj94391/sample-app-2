package com.example.instagram.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.DateTime
import com.example.instagram.ImageUtil
import com.example.instagram.TimeFormatting
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Likes
import com.example.instagram.database.entity.Location
import com.example.instagram.database.entity.SavedPost
import com.example.instagram.database.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProfilePostViewModel(private val currentProfile: Long, app: Application) : AndroidViewModel(app) {
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    private val imageUtil = ImageUtil(app)
    val listOfPosts: MutableList<Post> = mutableListOf()
    private var getImagesJob: Job? = null
    val newPostsLoaded = MutableLiveData<Int>()
    var isFirstTime = true
    
    
    fun addPostsToList(profileId: Long, type: Int, limit: Int, itemCount: Int) {
        if (getImagesJob != null) return
        getImagesJob = viewModelScope.launch(Dispatchers.IO) {
            val tempList: MutableList<Post> = mutableListOf()
            val postIds = if (type == 0) {
                db.postDao().getAllPosts(profileId, limit, itemCount)
            } else {
                db.tagPeopleDao().getAllTaggedPostOfProfile(profileId, limit, itemCount)
            }
            for (i in postIds) {
                tempList.add(getPost(i))
            }
            listOfPosts.addAll(tempList)
            /*delay(120)*/
            newPostsLoaded.postValue(tempList.size)
            getImagesJob = null
        }
    }
    
    private suspend fun getPost(postId: Long): Post {
        val profileId = getProfileId(postId)
        val profImageUrl = db.cacheDao().getCachedProfileImage(profileId) ?: imageUtil.getProfilePictureUrl(profileId) ?: ""
        
        val profileUsername = getProfileUserName(postId)
        val listOfPostPhotos = db.cacheDao().getCachedPostImages(postId)
        val isPostAlreadyLiked = getPostLikeStat(postId, currentProfile)
        val isPostAlreadySaved = getPostSaveStat(postId, currentProfile)
        val likeCount = getFormattedLikeCount(postId)
        val postDesc = getPostDesc(postId)
        val postTime = getFormattedTimeOfPost(postId)
        
        val post = Post(
            postId = postId,
            profileId = profileId,
            profileImageUrl = profImageUrl,
            profileUsername = profileUsername,
            listOfPostPhotos = listOfPostPhotos,
            isPostAlreadyLiked = isPostAlreadyLiked,
            isPostAlreadySaved = isPostAlreadySaved,
            likeCount = likeCount,
            postDesc = postDesc,
            timeOfPost = postTime,
            location = getLocation(postId)
        )
        return post
    }
    
    fun likePost(postId: Long, profileId: Long) {
        viewModelScope.launch {
            db.likesDao().insertNewLike(Likes(postId, profileId, System.currentTimeMillis()))
        }
    }
    
    fun removeLike(postId: Long, profileId: Long) {
        viewModelScope.launch {
            db.likesDao().deleteLike(profileId, postId)
        }
    }
    
    private suspend fun getLocation(postId: Long): Location? {
        val locationId = db.postDao().getLocationId(postId)
        return locationId?.let { db.locationDao().getLocation(it) }
    }
    
    fun savePost(profileId: Long, postId: Long) {
        viewModelScope.launch {
            db.savedPostDao().savePost(SavedPost(profileId, postId, System.currentTimeMillis()))
        }
    }
    
    fun removeSavedPost(profileId: Long, postId: Long) {
        viewModelScope.launch {
            db.savedPostDao().deleteSavedPost(postId, profileId)
        }
    }
    
    private suspend fun getFormattedTimeOfPost(postId: Long): String {
        val time = db.postDao().getPostTime(postId)
        return DateTime.timeFormatter(time, TimeFormatting.POST)
    }
    
    suspend fun getFormattedLikeCount(postId: Long): String {
        val count = db.likesDao().likeCount(postId)
        return if (count > 1) {
            "$count likes"
        } else {
            "$count like"
        }
    }
    
    private suspend fun getPostDesc(postId: Long): String {
        return db.postTextDao().getPostText(postId)
    }
    
    private suspend fun getProfileUserName(postId: Long): String {
        return db.postDao().getUsername(postId)
    }
    
    private suspend fun getPostLikeStat(postId: Long, profileId: Long): Boolean {
        return db.likesDao().isPostLikedByProfile(postId, profileId) > 0
    }
    
    suspend fun deletePost(postId: Long) = db.postDao().deletePost(postId)
    
    
    private suspend fun getPostSaveStat(postId: Long, profileId: Long): Boolean {
        return db.savedPostDao().isPostSavedByProfile(profileId, postId) > 0
    }
    
    private suspend fun getProfileId(postId: Long): Long {
        return db.postDao().getProfileId(postId)
    }
    
}