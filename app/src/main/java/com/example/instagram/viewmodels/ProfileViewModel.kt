package com.example.instagram.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.database.AppDatabase

class ProfileViewModel(private val app: Application) : AndroidViewModel(app) {
    private val db: AppDatabase = AppDatabase.getDatabase(app)
     val usersPost = MutableLiveData<MutableList<String>>()
     val usersTaggedPost = MutableLiveData<MutableList<String>>()

}