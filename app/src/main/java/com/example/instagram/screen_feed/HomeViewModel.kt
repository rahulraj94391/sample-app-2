package com.example.instagram.screen_feed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.data.common_model.Post
import com.example.instagram.domain.repo_contract.PostRepo
import com.example.instagram.screen_feed.usecase.GetPostUseCase
import com.example.instagram.screen_feed.usecase.LikeUseCase
import com.example.instagram.screen_feed.usecase.SavePostUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel(
    private val likeUseCase: LikeUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val getPostUseCase: GetPostUseCase,
    private val postRepo: PostRepo,
    private val imageUtil: ImageUtil,
) : ViewModel() {
    private var getImagesJob: Job? = null
    var isFirstTime = true
    val newPostsLoaded = MutableLiveData<Int>()
    val listOfPosts: MutableList<Post> = mutableListOf()
    
    fun addNewPostToList(loggedInProfileId: Long, limit: Int, itemCount: Int) {
        if (getImagesJob != null) return // checks if the previous job is of adding post is completed or not.
        
        getImagesJob = viewModelScope.launch(Dispatchers.IO) {
            val tempList: MutableList<Post> = mutableListOf()
            val postsToShowOnHome = postRepo.getPostOfFollowers(loggedInProfileId, limit, itemCount)
            for (postId in postsToShowOnHome) {
                val post = getPostUseCase(postId)
                if (post.profileImageUrl == null) {
                    post.profileImageUrl = imageUtil.getProfilePictureUrl(post.profileId) ?: ""
                }
                tempList.add(post)
            }
            listOfPosts.addAll(tempList)
            newPostsLoaded.postValue(tempList.size)
            getImagesJob = null
        }
    }
    
    fun likePost(postId: Long, profileId: Long) {
        viewModelScope.launch {
            likeUseCase.likePost(postId, profileId)
        }
    }
    
    fun removeLike(postId: Long, profileId: Long) {
        viewModelScope.launch {
            likeUseCase.removeLike(postId, profileId)
        }
    }
    
    fun savePost(profileId: Long, postId: Long) {
        viewModelScope.launch {
            savePostUseCase.savePost(profileId, postId)
        }
    }
    
    fun removeSavedPost(profileId: Long, postId: Long) {
        viewModelScope.launch {
            savePostUseCase.removeSavedPost(profileId, postId)
        }
    }
}