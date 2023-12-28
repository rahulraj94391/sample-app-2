package com.example.instagram.screen_profile.screen_viewPagerGridView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.common_model.PostPreview
import kotlinx.coroutines.async

class PhotoGridViewModel(app: Application) : AndroidViewModel(app) {
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    val usersPost = MutableLiveData<MutableList<PostPreview>>()
    val imageUtil = ImageUtil(app)
    val usersTaggedPost = MutableLiveData<MutableList<PostPreview>>()
    private var isUserBlocked = false
    
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
        val onePhotoPerPost = mutableListOf<PostPreview>()
        
        for (postId in postIds) {
            val photoLink = try {
                db.cacheDao().getFirstImgFromEachPost(postId) ?: imageUtil.getOneImagePerPost(mutableListOf(postId))[0].imageURl
            } catch (e: Exception) {
                ""
            }
            val onePhoto = PostPreview(postId, photoLink)
            onePhotoPerPost.add(onePhoto)
        }
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
        
        val onePhotoPerPost = mutableListOf<PostPreview>()
        for (postId in postIds) {
            val photoLink = db.cacheDao().getFirstImgFromEachPost(postId) ?: ""
            onePhotoPerPost.add(PostPreview(postId, photoLink))
        }
        usersTaggedPost.postValue(onePhotoPerPost)
    }
    
    private suspend fun isUserBlocked(ownProfileId: Long, userProfileId: Long): Boolean {
        val count = db.blockDao().isBlocked(ownProfileId, userProfileId)
        val isBlocked = count > 0
        isUserBlocked = isBlocked
        return isBlocked
    }
}