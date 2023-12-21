package com.example.instagram.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.instagram.ImageUtil
import com.example.instagram.domain.repo_contract.PostRepo
import com.example.instagram.domain.usecase.GetPostUseCase
import com.example.instagram.domain.usecase.LikeUseCase
import com.example.instagram.domain.usecase.SavePostUseCase
import com.example.instagram.viewmodels.HomeFragViewModel

class HomeViewModelFactory(
    private val likeUseCase: LikeUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val getPostUseCase: GetPostUseCase,
    private val postRepo: PostRepo,
    private val imageUtil: ImageUtil
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeFragViewModel(likeUseCase, savePostUseCase, getPostUseCase, postRepo, imageUtil) as T
    }
}