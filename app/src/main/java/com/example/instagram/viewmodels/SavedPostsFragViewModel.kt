package com.example.instagram.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.OnePhotoPerPost

class SavedPostsFragViewModel(app: Application) : AndroidViewModel(app) {
    private val imageUtil = ImageUtil(app)
    private val db = AppDatabase.getDatabase(app)
    val listOfSavedPosts = MutableLiveData<MutableList<OnePhotoPerPost>>()
    
    suspend fun getSavedPosts(profileId: Long) {
        val savedPostIds = db.savedPostDao().getAllSavedPosts(profileId)
//        val oneImageAssociatedWithPostId = imageUtil.getOneImagePerPost(savedPostIds)
        val oneImageAssociatedWithPostId = mutableListOf<OnePhotoPerPost>()
        for(postId in savedPostIds){
            oneImageAssociatedWithPostId.add(OnePhotoPerPost(postId, db.cacheDao().getFirstImgFromEachPost(postId)?: ""))
        }
        listOfSavedPosts.postValue(oneImageAssociatedWithPostId)
    }
}


