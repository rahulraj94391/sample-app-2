package com.example.instagram.screen_samePlaceIdPhotosView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.RECYCLER_VIEW_LIMIT
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.entity.Location
import com.example.instagram.data.common_model.PostPreview

class SameLocationPhotosViewModel(val app: Application) : AndroidViewModel(app) {
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    val postsFromSameLocation = mutableListOf<PostPreview>()
    val postRetrieved = MutableLiveData<Int>()
    val imageUtil: ImageUtil = ImageUtil(app)
    val currentLocation = MutableLiveData<Location>()
    
    suspend fun getPostsOfSameLocation(placeId: String, loggedInId: Long, itemCount: Int) {
        val temp = mutableListOf<PostPreview>()
        val postIds = db.postDao().getPostIdsFromPlaceId(placeId, loggedInId, RECYCLER_VIEW_LIMIT, itemCount)
        for (postId in postIds) {
            temp.add(getPost(postId))
        }
        postsFromSameLocation.addAll(temp)
        postRetrieved.postValue(temp.size)
    }
    
    private suspend fun getPost(postId: Long): PostPreview {
        val photoUrl = db.cacheDao().getFirstImgFromEachPost(postId) ?: return imageUtil.getOneImagePerPost(arrayListOf(postId))[0]
        return PostPreview(postId, photoUrl)
    }
    
    suspend fun getLocation(placeId: String) {
        val location = db.locationDao().getLocation(placeId)
        currentLocation.postValue(location)
    }
}