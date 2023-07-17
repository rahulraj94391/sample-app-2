package com.example.instagram.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.MSharedPreferences
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.SearchResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class SearchFragViewModel(private val application: Application) : AndroidViewModel(application) {
    val searchLiveData = MutableLiveData<MutableList<SearchResult>>()
    val imagesLiveData = MutableLiveData<MutableList<String>>()
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()


    suspend fun getSearchResults(name: String) {
        val sharedPref = application.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val ownID = sharedPref.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
        val db = AppDatabase.getDatabase(application)

        // get profile_id, username, first_name, last_name
        val searchResFormDB = viewModelScope.async {
            db.searchDao().getSearchResult(name, ownID)
        }
        val finalResult = searchResFormDB.await()


        // get images from firebase
        /* Do the implementation of firebase here. */
        val imageUrlAsync = viewModelScope.async {
            val tempList: MutableList<String> = mutableListOf()
            for (i in finalResult) {
                val profileId = i.profile_id
                val img = getProfilePicture(profileId)
                tempList.add(img.toString())
            }
            tempList
        }
        imagesLiveData.postValue(imageUrlAsync.await())
        searchLiveData.postValue(finalResult)

    }

    private suspend fun getProfilePicture(profileId: Long): String? {
        var profileImageUrl: String? = null
        val snapShot = firebaseFireStore
            .collection("profileImages")
            .whereEqualTo("ppid", "$profileId")
            .get()
            .await()
        for (i in snapShot) {
            profileImageUrl = i.data["$profileId"].toString()
            break
        }
        return profileImageUrl
    }

}