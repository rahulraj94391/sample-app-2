package com.example.instagram.viewModelFactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.instagram.viewmodels.ProfilePostViewModel

class ProfilePostViewModelFactory(private val currentProfile: Long, private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProfilePostViewModel(currentProfile, app) as T
    }
}