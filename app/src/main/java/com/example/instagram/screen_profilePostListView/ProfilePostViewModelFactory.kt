package com.example.instagram.screen_profilePostListView

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProfilePostViewModelFactory(private val currentProfile: Long, private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProfilePostImagesViewModel(currentProfile, app) as T
    }
}