package com.example.instagram.screen_savedPosts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.common_model.PostPreview

class SavedPostsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getDatabase(app)
    val listOfSavedPosts = MutableLiveData<MutableList<PostPreview>>()
    
    suspend fun getSavedPosts(profileId: Long) {
        val savedPostIds = db.savedPostDao().getAllSavedPosts(profileId)
        val oneImageAssociatedWithPostId = mutableListOf<PostPreview>()
        for(postId in savedPostIds){
            oneImageAssociatedWithPostId.add(PostPreview(postId, db.cacheDao().getFirstImgFromEachPost(postId)?: ""))
        }
        listOfSavedPosts.postValue(oneImageAssociatedWithPostId)
    }
}


