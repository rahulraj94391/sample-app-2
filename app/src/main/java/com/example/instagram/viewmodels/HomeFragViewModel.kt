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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "HomeFragViewModel_CommTag"

class HomeFragViewModel(private val currentProfile: Long, private val app: Application) : AndroidViewModel(app) {
    var isFirstTime = true
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    private val imageUtil = ImageUtil(app)
    val postsToShow = MutableLiveData<MutableList<Post>>()
    private val postIdsAlreadyShown = mutableSetOf<Long>()
    val listOfPosts: MutableList<Post>? = null
    
    fun addNewPostToList(loggedInProfileId: Long) {
        postIdsAlreadyShown.clear()
        viewModelScope.launch {
            val tempList: MutableList<Post> = mutableListOf()
            val postsToShowOnHome = db.postDao().getPostOfFollowers(loggedInProfileId)
            for (i in postsToShowOnHome) {
                if (!postIdsAlreadyShown.contains(i)) {
                    postIdsAlreadyShown.add(i)
                    tempList.add(getPost(i))
                }
            }
            postsToShow.postValue(tempList)
        }
    }
    
    private suspend fun getPost(postId: Long): Post {
        val profileId = viewModelScope.async { getProfileId(postId) }
        val profImageUrl = viewModelScope.async { imageUtil.getProfilePictureUrl(profileId.await()) }
        val profileUsername = viewModelScope.async { getProfileUserName(postId) }
        val listOfPostPhotos = viewModelScope.async { imageUtil.getPostImages(postId) }
        val isPostAlreadyLiked = viewModelScope.async { getPostLikeStat(postId, currentProfile) }
        val isPostAlreadySaved = viewModelScope.async { getPostSaveStat(postId, currentProfile) }
        val likeCount = viewModelScope.async { getFormattedLikeCount(postId) }
        val postDesc = viewModelScope.async { getPostDesc(postId) }
        val postTime = viewModelScope.async { getFormattedTimeOfPost(postId) }
        
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
        
        //        Log.d(TAG, "Post generated = $post")
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
        return "${db.likesDao().likeCount(postId)} like"
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