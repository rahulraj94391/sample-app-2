package com.example.instagram.screen_searchUser

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.common.util.MSharedPreferences
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.entity.RecentSearch
import com.example.instagram.screen_searchUser.model.UserSearchResult
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SearchViewModel(private val app: Application) : AndroidViewModel(app) {
    val imageUtil = ImageUtil(app)
    val db = AppDatabase.getDatabase(app)
    val searchLiveData = MutableLiveData<MutableList<UserSearchResult>>()
    val imagesLiveData = MutableLiveData<MutableList<String>>()
    
    suspend fun getSearchResults(name: String) {
        val sharedPref = app.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val ownID = sharedPref.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
        
        // get profile_id, username, first_name, last_name
        val searchResFormDB = viewModelScope.async {
            db.searchDao().getSearchResult(name, ownID)
        }
        val finalResult = searchResFormDB.await()
        
        // get images from firebase
        val imageUrlAsync = viewModelScope.async {
            val tempList: MutableList<String> = mutableListOf()
            for (i in finalResult) {
                val img = imageUtil.getProfilePictureUrl(i.profile_id)
                tempList.add(img.toString())
            }
            tempList
        }
        imagesLiveData.postValue(imageUrlAsync.await())
        searchLiveData.postValue(finalResult)
    }
    
    fun addNameToRecentSearch(id: Long, name: String, ownerId: Long) = viewModelScope.launch {
        db.recentSearchDao().insertAndDeleteIfExist(RecentSearch(id, name, ownerId))
    }
    
    fun deleteAllFromRecent() = viewModelScope.launch {
        db.recentSearchDao().deleteAllSearches()
    }
}