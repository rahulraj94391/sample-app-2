package com.example.instagram.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.BlockedUsers
import com.example.instagram.database.model.ProfileSummary
import kotlinx.coroutines.async

private const val TAG = "CommTag_ProfileViewModel"

class ProfileFragViewModel(app: Application) : AndroidViewModel(app) {
    private val imageUtil = ImageUtil(app)
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    var profileSummary = MutableLiveData<ProfileSummary>()
    var isUserBlocked = false
        private set
    
    suspend fun getProfileSummary(myId: Long, userId: Long) {
        val fullNameBio = viewModelScope.async { db.profileDao().getFullNameBio(userId) }
        val username = viewModelScope.async { db.loginCredDao().getUsername(userId) }
        if (isUserBlocked(myId, userId)) {
            val profSummary = ProfileSummary(
                username.await(),
                "",
                fullNameBio.await().first_name,
                fullNameBio.await().last_name,
                "---",
                0,
                0,
                0,
                false
            )
            profileSummary.postValue(profSummary)
            return
        }
        
        val profilePic = viewModelScope.async {
            db.cacheDao().getCachedProfileImage(userId) ?: imageUtil.getProfilePictureUrl(userId) ?: ""
        }
        val postCount = viewModelScope.async { db.postDao().getPostCount(userId) }
        val followerCount = viewModelScope.async { db.followDao().getFollowerCount(userId) }
        val followingCount = viewModelScope.async { db.followDao().getFollowingCount(userId) }
        val isFollowing = viewModelScope.async { db.followDao().isUserFollowingUser(myId, userId) > 0 }
        
        val profSummary = ProfileSummary(
            username.await(),
            profilePic.await(),
            fullNameBio.await().first_name,
            fullNameBio.await().last_name,
            fullNameBio.await().bio,
            postCount.await(),
            followerCount.await(),
            followingCount.await(),
            isFollowing.await()
        )
        profileSummary.postValue(profSummary)
    }
    
    private suspend fun isUserBlocked(myId: Long, userId: Long): Boolean {
        val count = db.blockDao().isBlocked(myId, userId)
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
    
    suspend fun unblockUser(myId: Long, userId: Long) {
        val isDeleted = db.blockDao().unblockUser(myId, userId)
        if (isDeleted < 1) return
        getProfileSummary(myId, userId)
    }
    
    suspend fun blockUser(myId: Long, userId: Long) {
        db.blockDao().blockUser(BlockedUsers(myId, userId))
        db.followDao().deleteFollow(userId, myId)
        db.followDao().deleteFollow(myId, userId)
        db.recentSearchDao().deleteIfExist(myId, userId)
        getProfileSummary(myId, userId)
    }
}