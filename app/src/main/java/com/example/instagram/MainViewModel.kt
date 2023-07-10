package com.example.instagram

import androidx.lifecycle.ViewModel
import com.example.instagram.database.entity.Profile

class MainViewModel() : ViewModel() {
    var newProfileSignup: Profile? = null
    var loggedInUserId: Long? = null


    /**
     * this variable is used to identify which profile to open when visiting profile screen.
     *
     * if this is null then opening 'loggedInUserId' profile page.
     */
    var profilePageId: Long? = null
}