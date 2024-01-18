package com.example.instagram.screen_feed

import android.content.Context
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.local_repo.CacheRepoImpl
import com.example.instagram.data.local_repo.CommentRepoImpl
import com.example.instagram.data.local_repo.LikeRepoImpl
import com.example.instagram.data.local_repo.LocationRepoImpl
import com.example.instagram.data.local_repo.PostRepoImpl
import com.example.instagram.data.local_repo.PostTextRepoImpl
import com.example.instagram.data.local_repo.SavedPostRepoImpl
import com.example.instagram.screen_feed.usecase.GetPostUseCase
import com.example.instagram.screen_feed.usecase.LikeUseCase
import com.example.instagram.screen_feed.usecase.RemoveLikeUseCase
import com.example.instagram.screen_feed.usecase.RemoveSavePostUseCase
import com.example.instagram.screen_feed.usecase.SavePostUseCase

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
    val removeLikeUseCase = RemoveLikeUseCase(likeRepo)
    val savePostUseCase = SavePostUseCase(savedPostRepo)
    val removeSavePostUseCase = RemoveSavePostUseCase(savedPostRepo)
    
    
    // image util
    val imageUtil = ImageUtil(context)
    
    
    // VM Factory - Owner: HomeFragment
    val viewModelFactory = HomeViewModelFactory(likeUseCase, removeLikeUseCase, savePostUseCase, removeSavePostUseCase, getPostUseCase, postRepo, imageUtil)
}