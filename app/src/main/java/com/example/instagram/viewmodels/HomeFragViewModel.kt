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
import com.example.instagram.database.entity.SavedPost
import com.example.instagram.database.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "HomeFragViewModel_CommTag"

class HomeFragViewModel(private val currentProfile: Long, app: Application) : AndroidViewModel(app) {
    var isFirstTime = true
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    private val imageUtil = ImageUtil(app)
    val newPostsLoaded = MutableLiveData<Int>()
    private var getImagesJob: Job? = null
    val listOfPosts: MutableList<Post> = mutableListOf()
    
    fun addNewPostToList(loggedInProfileId: Long, limit: Int, itemCount: Int) {
        if (getImagesJob != null) return
        getImagesJob = viewModelScope.launch(Dispatchers.IO) {
            val tempList: MutableList<Post> = mutableListOf()
            val postsToShowOnHome = db.postDao().getPostOfFollowers(loggedInProfileId, limit, itemCount)
            for (i in postsToShowOnHome) {
                tempList.add(getPost2(i))
            }
            listOfPosts.addAll(tempList)
            newPostsLoaded.postValue(tempList.size)
            getImagesJob = null
        }
    }
    
    private suspend fun getPost2(postId: Long): Post {
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
            timeOfPost = postTime
        )
        return post
    }
    
    private suspend fun getPost(postId: Long): Post {
        val profileId = viewModelScope.async(Dispatchers.IO) { getProfileId(postId) }
        val profImageUrl = viewModelScope.async(Dispatchers.IO) {
            // imageUtil.getProfilePictureUrl(profileId.await())
            db.cacheDao().getCachedProfileImage(profileId.await()) ?: imageUtil.getProfilePictureUrl(profileId.await()) ?: ""
        }
        val profileUsername = viewModelScope.async(Dispatchers.IO) { getProfileUserName(postId) }
        val listOfPostPhotos = viewModelScope.async(Dispatchers.IO) {
            db.cacheDao().getCachedPostImages(postId)
        }
        val isPostAlreadyLiked = viewModelScope.async(Dispatchers.IO) { getPostLikeStat(postId, currentProfile) }
        val isPostAlreadySaved = viewModelScope.async(Dispatchers.IO) { getPostSaveStat(postId, currentProfile) }
        val likeCount = viewModelScope.async(Dispatchers.IO) { getFormattedLikeCount(postId) }
        val postDesc = viewModelScope.async(Dispatchers.IO) { getPostDesc(postId) }
        val postTime = viewModelScope.async(Dispatchers.IO) { getFormattedTimeOfPost(postId) }
        
        val post = Post(
            postId = postId,
            profileId = profileId.await(),
            profileImageUrl = profImageUrl.await(),
            profileUsername = profileUsername.await(),
            listOfPostPhotos = listOfPostPhotos.await(),
            isPostAlreadyLiked = isPostAlreadyLiked.await(),
            isPostAlreadySaved = isPostAlreadySaved.await(),
            likeCount = likeCount.await(),
            postDesc = postDesc.await(),
            timeOfPost = postTime.await()
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
    
    private suspend fun getPostSaveStat(postId: Long, profileId: Long): Boolean {
        return db.savedPostDao().isPostSavedByProfile(profileId, postId) > 0
    }
    
    private suspend fun getProfileId(postId: Long): Long {
        return db.postDao().getProfileId(postId)
    }
}