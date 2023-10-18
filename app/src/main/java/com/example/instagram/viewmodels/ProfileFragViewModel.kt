package com.example.instagram.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.ProfileSummary
import kotlinx.coroutines.async

private const val TAG = "CommTag_ProfileViewModel"

class ProfileFragViewModel(app: Application) : AndroidViewModel(app) {
    private val imageUtil = ImageUtil(app)
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    var profileSummary = MutableLiveData<ProfileSummary>()
    
    suspend fun getProfileSummary(ownProfileId: Long, userProfileId: Long) {
        val profilePic = viewModelScope.async {
            imageUtil.getProfilePictureUrl(userProfileId)
        }
        val fullNameBio = viewModelScope.async { db.profileDao().getFullNameBio(userProfileId) }
        val postCount = viewModelScope.async { db.postDao().getPostCount(userProfileId) }
        val followerCount = viewModelScope.async { db.followDao().getFollowerCount(userProfileId) }
        val followingCount = viewModelScope.async { db.followDao().getFollowingCount(userProfileId) }
        val username = viewModelScope.async { db.loginCredDao().getUsername(userProfileId) }
        val isFollowing = viewModelScope.async { db.followDao().isUserFollowingUser(ownProfileId, userProfileId) > 0 }
        
        val profSummary = ProfileSummary(
            username.await(),
            profilePic.await().also {
                Log.d(TAG, "$it")
            },
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
}