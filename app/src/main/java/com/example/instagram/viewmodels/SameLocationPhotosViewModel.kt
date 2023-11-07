package com.example.instagram.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Location
import com.example.instagram.database.model.OnePhotoPerPost

private const val TAG = "SameLocationVM_CommTag"

class SameLocationPhotosViewModel(val app: Application) : AndroidViewModel(app) {
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    val postsFromSameLocation = mutableListOf<OnePhotoPerPost>()
    val postRetrieved = MutableLiveData<Int>()
    val imageUtil: ImageUtil = ImageUtil(app)
    val currentLocation = MutableLiveData<Location>()
    
    suspend fun getPostsOfSameLocation(placeId: String, loggedInId: Long, itemCount: Int) {
        val temp = mutableListOf<OnePhotoPerPost>()
        val postIds = db.postDao().getPostIdsFromPlaceId(placeId, loggedInId, 9, itemCount)
        Log.d(TAG, "postIds: $postIds")
        for (postId in postIds) {
            temp.add(getPost(postId))
        }
        postsFromSameLocation.addAll(temp)
        postRetrieved.postValue(temp.size)
    }
    
    private suspend fun getPost(postId: Long): OnePhotoPerPost {
        val photoUrl = db.cacheDao().getFirstImgFromEachPost(postId) ?: return imageUtil.getOneImagePerPost(arrayListOf(postId))[0]
        return OnePhotoPerPost(postId, photoUrl)
    }
    
    suspend fun getLocation(placeId: String) {
        val location = db.locationDao().getLocation(placeId)
        currentLocation.postValue(location)
    }
    
}