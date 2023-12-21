package com.example.instagram.di

import android.content.Context
import com.example.instagram.ImageUtil
import com.example.instagram.data.repo.CacheRepoImpl
import com.example.instagram.data.repo.CommentRepoImpl
import com.example.instagram.data.repo.LikeRepoImpl
import com.example.instagram.data.repo.LocationRepoImpl
import com.example.instagram.data.repo.PostRepoImpl
import com.example.instagram.data.repo.PostTextRepoImpl
import com.example.instagram.data.repo.SavedPostRepoImpl
import com.example.instagram.database.AppDatabase
import com.example.instagram.domain.usecase.GetPostUseCase
import com.example.instagram.domain.usecase.LikeUseCase
import com.example.instagram.domain.usecase.SavePostUseCase
import com.example.instagram.viewModelFactory.HomeViewModelFactory

class HomeScreenDependencies(context: Context, loggedInProfileId: Long) {
    // init repos
    val db = AppDatabase.getDatabase(context)
    val cacheRepo = CacheRepoImpl(db.cacheDao())
    val postRepo = PostRepoImpl(db.postDao())
    val locationRepo = LocationRepoImpl(db.locationDao())
    val postTextRepo = PostTextRepoImpl(db.postTextDao())
    val likeRepo = LikeRepoImpl(db.likesDao())
    val savedPostRepo = SavedPostRepoImpl(db.savedPostDao())
    val commentRepo = CommentRepoImpl(db.commentDao())
    
    
    //use cases
    val getPostUseCase = GetPostUseCase(loggedInProfileId, cacheRepo, postRepo, locationRepo, postTextRepo, likeRepo, savedPostRepo)
    val likeUseCase = LikeUseCase(likeRepo)
    val savePostUseCase = SavePostUseCase(savedPostRepo)
    
    
    // image util
    val imageUtil = ImageUtil(context)
    
    
    // VM Factory - Owner: HomeFragment
    val viewModelFactory = HomeViewModelFactory(likeUseCase, savePostUseCase, getPostUseCase, postRepo, imageUtil)
}