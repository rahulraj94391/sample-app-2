package com.example.instagram.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.FollowList
import com.example.instagram.fragments.TYPE_FOLLOWER
import kotlinx.coroutines.launch

class ListFollowFragViewModel(private val app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getDatabase(app)
    val users = MutableLiveData<MutableList<FollowList>>()
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
            followerList[i].photoUrl = imageUtil.getProfilePicture(followerList[i].profile_id)
        }
        users.postValue(followerList)
    }
    
    private suspend fun getFollowing(profileId: Long) {
        val followerList = db.followDao().getFollowingList(profileId)
        for (i in followerList.indices) {
            followerList[i].photoUrl = imageUtil.getProfilePicture(followerList[i].profile_id)
        }
        users.postValue(followerList)
    }
}