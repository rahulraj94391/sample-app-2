package com.example.instagram

import androidx.lifecycle.ViewModel
import com.example.instagram.database.entity.Profile

class MainViewModel() : ViewModel() {
    var newProfileSignup: Profile? = null
    var loggedInUserId: Long? = null
}