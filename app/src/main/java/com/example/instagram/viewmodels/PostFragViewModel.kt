package com.example.instagram.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.instagram.ImageUtil
import com.example.instagram.MSharedPreferences
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.SearchResult
import com.example.instagram.worker.POST_TAGS_KEY
import com.example.instagram.worker.POST_TEXT_KEY
import com.example.instagram.worker.PROFILE_ID_KEY
import com.example.instagram.worker.UPLOAD_IMAGE_PATH_KEY
import com.example.instagram.worker.UploadPostPictures
import com.google.android.material.chip.Chip
import kotlinx.coroutines.async

private const val TAG = "CommTag_PostFragmentViewModel"

class PostFragViewModel(private val app: Application) : AndroidViewModel(app) {
    val imageUtil = ImageUtil(app)
    val imagesLiveData = MutableLiveData<MutableList<String>>()
    var postImagesUri: MutableList<Uri> = mutableListOf()
    var profileId: Long = -1
    var tempListTagUser = MutableLiveData<MutableList<SearchResult>>()
    var finalTagUserIds = mutableListOf<Pair<Chip, Long>>()
    var tagsToUpload = mutableListOf<Long>()
    var finalTextToUpload = ""
    
    init {
        val sharedPreferences = app.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        profileId = sharedPreferences.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
    }
    
    fun insertPost() {
        uploadPostWork(uriToStringArray(postImagesUri))
        
        // clear all variables after inserting.
        postImagesUri = mutableListOf()
        finalTagUserIds.clear()
        tagsToUpload.clear()
    }
    
    private fun uriToStringArray(postImagesUri: MutableList<Uri>): Array<String> {
        val images = mutableListOf<String>()
        for (i in postImagesUri.indices) {
            val uri = postImagesUri[i]
            images.add(uri.toString())
        }
        return images.toTypedArray()
    }
    
    private fun uploadPostWork(uris: Array<String>) {
        val data = Data.Builder()
            .putString(POST_TEXT_KEY, finalTextToUpload)
            .putLong(PROFILE_ID_KEY, profileId)
            .putStringArray(UPLOAD_IMAGE_PATH_KEY, uris)
            .putLongArray(POST_TAGS_KEY, tagsToUpload.toLongArray())
            .build()
        
        
        val oneTimeWorkRequest = OneTimeWorkRequest
            .Builder(UploadPostPictures::class.java)
            .setInputData(data)
            .build()
        
        WorkManager.getInstance(app).enqueue(oneTimeWorkRequest)
    }
    
    suspend fun getSearchResult(name: String) {
        val sharedPref = app.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val ownID = sharedPref.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
        val db = AppDatabase.getDatabase(app)
        
        val usersRes = viewModelScope.async {
            db.searchDao().getSearchResult(name, ownID)
        }
        val searchResWOPhoto = usersRes.await()
        
        val listOfImages = mutableListOf<String>()
        for (i in searchResWOPhoto.indices) {
            listOfImages.add(imageUtil.getProfilePicture(searchResWOPhoto[i].profile_id) ?: "")
        }
        
        tempListTagUser.postValue(searchResWOPhoto)
        imagesLiveData.postValue(listOfImages)
    }
    
    suspend fun getNameOfUser(profileId: Long): String {
        val db = AppDatabase.getDatabase(app)
        val fullName = db.profileDao().getFullName(profileId)
        return "${fullName.first_name} ${fullName.last_name}"
    }
}