package com.example.instagram.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.OnePhotoPerPost

class HashTagFragViewModel(app: Application) : AndroidViewModel(app) {
    val photos: MutableList<OnePhotoPerPost> = mutableListOf()
    private val db = AppDatabase.getDatabase(app)
    val listOfPostFromSameHashTag = MutableLiveData<Int>()
    
    suspend fun getPosts(hashTag: String, myId: Long, limit: Int, offset: Int) {
        val postIds = db.hashtagDao().getPostIds(hashTag, myId, limit, offset)
        val temp = mutableListOf<OnePhotoPerPost>()
        for (postId in postIds) {
            temp.add(OnePhotoPerPost(postId, db.cacheDao().getFirstImgFromEachPost(postId) ?: ""))
        }
        photos.addAll(temp)
        listOfPostFromSameHashTag.postValue(temp.size)
    }
    
}