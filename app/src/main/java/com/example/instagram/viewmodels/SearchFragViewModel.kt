package com.example.instagram.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.MSharedPreferences
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.SearchResult
import kotlinx.coroutines.async

class SearchFragViewModel(private val application: Application) : AndroidViewModel(application) {
    val searchLiveData = MutableLiveData<MutableList<SearchResult>>()
    val imagesLiveData = MutableLiveData<MutableList<Bitmap>>()


    suspend fun getSearchResults(name: String) {
        val sharedPref = application.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val ownID = sharedPref.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
        val db = AppDatabase.getDatabase(application)

        // get profile_id, username, first_name, last_name
        val searchResFormDB = viewModelScope.async {
            db.searchDao().getSearchResult(name, ownID)
        }
        // get images from firebase
        /* Do the implementation of firebase here. */


        val finalResult = searchResFormDB.await()
        searchLiveData.postValue(finalResult)
        insertDummyData(name)
    }

    private fun insertDummyData(name: String) {
        val list = mutableListOf<SearchResult>()
        for (i in 0 until 60) {
            list.add(SearchResult((i).toLong(), name, "LastName $i", "username $i"))
        }
        searchLiveData.postValue(list)
    }

}