package com.example.instagram.screen_hashTagView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.common_model.PostPreview

class HashTagFragViewModel(app: Application) : AndroidViewModel(app) {
    val photos: MutableList<PostPreview> = mutableListOf()
    private val db = AppDatabase.getDatabase(app)
    val listOfPostFromSameHashTag = MutableLiveData<Int>()
    
    suspend fun getPosts(hashTag: String, myId: Long, limit: Int, offset: Int) {
        val postIds = db.hashtagDao().getPostIds(hashTag, myId, limit, offset)
        val temp = mutableListOf<PostPreview>()
        for (postId in postIds) {
            temp.add(PostPreview(postId, db.cacheDao().getFirstImgFromEachPost(postId) ?: ""))
        }
        photos.addAll(temp)
        listOfPostFromSameHashTag.postValue(temp.size)
    }
    
}