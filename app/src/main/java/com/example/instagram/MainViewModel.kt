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
    
    /*var profileOpenCount = 0*/
    
    /**
     * When we swipe down to refresh on ProfileFragment, PhotoGridFragment is observing this live data,
     * and give call to PhotoGridFragment's view-model to re-fetch logged-in profile photos.
     */
    val startProfileRefresh = MutableLiveData(false)
    
    /** This is observed by the ProfileFragment, when the re-fetching/refresh of photos is done by the
     * PhotoGridFragment's view-model then ProfileFragment sets ->
     * swipeRefreshLayout.isRefreshing = false
     */
    val isProfileRefreshed = MutableLiveData(false)
    
    
    /**
     * This is used when user moves from Following list to User Profile list.
     *
     * This stores the user profile position in the following list, when UNFOLLOW is
     * clicked on another user's profile, that user is removed from Following list.
     */
    val removeProfileFromFollowingList = MutableLiveData(-1)
    
    
    /**
     * PhotoGridFragment post value about the Post tapped by the user as Pair<Long, Int> [postId, pos(in adapter)].
     * Profile fragment is observing this and opens post based on the info provided in the Pair<Long, Int>.
     */
    val openPost = MutableLiveData(Pair(-1L, -1))
    
    val openPost2 = MutableLiveData(Pair(-1, -1)) // pair(type, pos)
    
}