package com.example.instagram.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.OnePhotoPerPost
import kotlinx.coroutines.async

private const val TAG = "CommTag_PhotoGridFragViewModel"

class PhotoGridFragViewModel(app: Application) : AndroidViewModel(app) {
    private val imageUtil = ImageUtil(app)
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    val usersPost = MutableLiveData<MutableList<OnePhotoPerPost>>()
    val usersTaggedPost = MutableLiveData<MutableList<OnePhotoPerPost>>()
    
    suspend fun getProfilePost(profileId: Long) {
        val postIds = viewModelScope.async {
            db.postDao().getAllPostOfProfile(profileId)
        }
        Log.d(TAG, "post id = ${postIds.await()}")
        val urlsOfOnePhotoPerPost = imageUtil.getOneImagePerPost(postIds.await())
        usersPost.postValue(urlsOfOnePhotoPerPost.asReversed())
    }
    
    suspend fun getAllPostWhereProfileIsTagged(profileId: Long) {
        val defList = viewModelScope.async {
            db.tagPeopleDao().getAllTaggedPostOfProfile(profileId)
        }
        val urlsOfOnePhotoPerPost = imageUtil.getOneImagePerPost(defList.await())
        usersTaggedPost.postValue(urlsOfOnePhotoPerPost.asReversed())
    }
}