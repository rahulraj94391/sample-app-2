package com.example.instagram

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.database.entity.Profile

class MainViewModel(app: Application) : AndroidViewModel(app) {
    
    
    var newProfileSignup: Profile? = null
    var loggedInProfileId: Long? = null
    
    
    /**
     * this variable is used to identify which profile to open when visiting profile screen.
     *
     * if this is null then opening 'loggedInUserId' profile page.
     */
    var profilePageId: Long? = null
    
    var profileImageBitmap: Bitmap? = null
    
    val startProfileRefresh = MutableLiveData(false)
    val isProfileRefreshed = MutableLiveData(false)
    
}