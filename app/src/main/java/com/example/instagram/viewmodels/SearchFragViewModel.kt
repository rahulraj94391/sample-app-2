package com.example.instagram.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.ImageUtil
import com.example.instagram.MSharedPreferences
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.SearchResult
import kotlinx.coroutines.async

class SearchFragViewModel(private val app: Application) : AndroidViewModel(app) {
    val imageUtil = ImageUtil(app)
    val searchLiveData = MutableLiveData<MutableList<SearchResult>>()
    val imagesLiveData = MutableLiveData<MutableList<String>>()
    
    suspend fun getSearchResults(name: String) {
        val sharedPref = app.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val ownID = sharedPref.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
        val db = AppDatabase.getDatabase(app)

        // get profile_id, username, first_name, last_name
        val searchResFormDB = viewModelScope.async {
            db.searchDao().getSearchResult(name, ownID)
        }
        val finalResult = searchResFormDB.await()
        
        // get images from firebase
        val imageUrlAsync = viewModelScope.async {
            val tempList: MutableList<String> = mutableListOf()
            for (i in finalResult) {
                val img = imageUtil.getProfilePicture(i.profile_id)
                tempList.add(img.toString())
            }
            tempList
        }
        imagesLiveData.postValue(imageUrlAsync.await())
        searchLiveData.postValue(finalResult)

    }
}