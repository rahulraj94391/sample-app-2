package com.example.instagram.screen_feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.domain.repo_contract.PostRepo
import com.example.instagram.screen_feed.usecase.GetPostUseCase
import com.example.instagram.screen_feed.usecase.LikeUseCase
import com.example.instagram.screen_feed.usecase.RemoveLikeUseCase
import com.example.instagram.screen_feed.usecase.RemoveSavePostUseCase
import com.example.instagram.screen_feed.usecase.SavePostUseCase

class HomeViewModelFactory(
    private val likeUseCase: LikeUseCase,
    private val removeLikeUseCase: RemoveLikeUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val removeSavePostUseCase: RemoveSavePostUseCase,
    private val getPostUseCase: GetPostUseCase,
    private val postRepo: PostRepo,
    private val imageUtil: ImageUtil,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(likeUseCase, removeLikeUseCase, savePostUseCase, removeSavePostUseCase, getPostUseCase, postRepo, imageUtil) as T
    }
}