package com.example.instagram.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.instagram.ImageUtil
import com.example.instagram.MSharedPreferences
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.TagSearchResult
import com.example.instagram.worker.POST_TAGS_KEY
import com.example.instagram.worker.POST_TEXT_KEY
import com.example.instagram.worker.PROFILE_ID_KEY
import com.example.instagram.worker.UPLOAD_IMAGE_PATH_KEY
import com.example.instagram.worker.UploadPostPictures
import kotlinx.coroutines.async
import java.util.UUID


private const val TAG = "CommTag_PostFragmentViewModel"

class PostFragViewModel(private val app: Application) : AndroidViewModel(app) {
    private val imageUtil = ImageUtil(app)
    val postImagesUri: MutableList<Uri> = mutableListOf()
    var profileId: Long = -1
    val tagsToUpload = mutableListOf<Long>()
    var finalTextToUpload = ""
    var uuidWorkReq = MutableLiveData<UUID>()
    
    // this will be used to display only selected tags
    val finalTags = mutableListOf<TagSearchResult>()
    
    // this list is used to display the search results of user
    val tagSearchResults = MutableLiveData<MutableList<TagSearchResult>>()
    
    init {
        val sharedPreferences = app.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        profileId = sharedPreferences.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
    }
    
    fun insertPost() {
        if (finalTags.isNotEmpty()) {
            finalTags.forEach {
                tagsToUpload.add(it.profile_id)
            }
        }
        
        uploadPostWork(uriToStringArray(postImagesUri))
        
        // clear all variables after inserting.
        tagsToUpload.clear()
        postImagesUri.clear()
        finalTags.clear()
        finalTextToUpload = ""
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
        
        val mConstraints = Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val oneTimeWorkRequest = OneTimeWorkRequest
            .Builder(UploadPostPictures::class.java)
            .setConstraints(mConstraints)
            .setInputData(data)
            .build()
        
        Log.d(TAG, "uploadPostWork: ${oneTimeWorkRequest.id}")
        
        WorkManager.getInstance(app).enqueue(oneTimeWorkRequest)
        uuidWorkReq.postValue(oneTimeWorkRequest.id)
    }
    
    
    suspend fun getSearchResults(name: String) {
        val sharedPref = app.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val ownID = sharedPref.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
        val db = AppDatabase.getDatabase(app)
        
        // get profile_id, username, first_name, last_name
        val searchResFormDB = viewModelScope.async {
            db.searchDao().getSearchResult(name, ownID)
        }
        val finalResultWoPicUrl = searchResFormDB.await()
        
        // get images from firebase
        val imageUrlFromFirebase = viewModelScope.async {
            val tempList: MutableList<String> = mutableListOf()
            for (i in finalResultWoPicUrl) {
                val img = imageUtil.getProfilePictureUrl(i.profile_id)
                tempList.add(img.toString())
            }
            tempList
        }
        
        
        val finalListWithImages = imageUrlFromFirebase.await()
        val finalListWithImage = mutableListOf<TagSearchResult>()
        
        for (i in finalResultWoPicUrl.indices) {
            val singleRes = TagSearchResult(
                finalResultWoPicUrl[i].profile_id,
                finalResultWoPicUrl[i].first_name,
                finalResultWoPicUrl[i].last_name,
                finalResultWoPicUrl[i].username,
                finalListWithImages[i]
            )
            finalListWithImage.add(singleRes)
        }
        tagSearchResults.postValue(finalListWithImage)
    }
}