package com.example.instagram.screen_followerAndFollowingView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.TYPE_FOLLOWER
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.data.AppDatabase
import com.example.instagram.screen_followerAndFollowingView.model.Connection
import kotlinx.coroutines.launch

class ConnectionsViewModel(private val app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getDatabase(app)
    val users = MutableLiveData<MutableList<Connection>>()
    private var imageUtil = ImageUtil(app)
    
    fun getUsers(type: String, profileId: Long) {
        if (type == TYPE_FOLLOWER) {
            viewModelScope.launch { getFollowers(profileId) }
        } else {
            viewModelScope.launch { getFollowing(profileId) }
        }
    }
    
    private suspend fun getFollowers(profileId: Long) {
        val followerList = db.followDao().getFollowerList(profileId)
        for (i in followerList.indices) {
            val profileUrl = db.cacheDao().getCachedProfileImage(followerList[i].profile_id) ?: imageUtil.getProfilePictureUrl(followerList[i].profile_id)
            followerList[i].photoUrl = profileUrl
        }
        users.postValue(followerList)
    }
    
    private suspend fun getFollowing(profileId: Long) {
        val followingList = db.followDao().getFollowingList(profileId)
        for (i in followingList.indices) {
            followingList[i].photoUrl = db.cacheDao().getCachedProfileImage(followingList[i].profile_id) ?: imageUtil.getProfilePictureUrl(followingList[i].profile_id)
        }
        users.postValue(followingList)
    }
}