package com.example.instagram.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.instagram.database.AppDatabase
import com.example.instagram.viewmodels.HomeFragViewModel

class ViewModelFactory(private val currentProfile: Long, private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeFragViewModel(currentProfile, db) as T
    }
}