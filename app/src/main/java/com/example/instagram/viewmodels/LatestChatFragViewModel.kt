package com.example.instagram.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.instagram.database.AppDatabase

class LatestChatFragViewModel(app: Application) : AndroidViewModel(app) {
    
    
    val db = AppDatabase.getDatabase(app)
    
}