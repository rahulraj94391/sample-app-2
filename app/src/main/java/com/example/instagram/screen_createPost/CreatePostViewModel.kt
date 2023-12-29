package com.example.instagram.screen_createPost

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.common.util.MSharedPreferences
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.entity.Location
import com.example.instagram.data.repo.LocationCacheRepoImpl
import com.example.instagram.screen_createPost.model.TagSearchResult
import com.example.instagram.screen_createPost.screen_locationTag.GetLocationUseCase
import kotlinx.coroutines.async
import java.util.UUID


class CreatePostViewModel(private val app: Application) : AndroidViewModel(app) {
    private val tagsToUpload = mutableListOf<Long>()
    private val imageUtil = ImageUtil(app)
    private val db = AppDatabase.getDatabase(app)
    val hashTagList = mutableListOf<String>()
    val isHashTagListUpdated = MutableLiveData(false)
    val postImagesUri: MutableList<Uri> = mutableListOf()
    var profileId: Long = -1
    var finalTextToUpload = ""
    var uuidWorkReq = MutableLiveData<UUID>()
    var locationTag: Location? = null
    var locations = mutableListOf<Location>()
    val getLocationUseCase = GetLocationUseCase(LocationCacheRepoImpl(db.locationCacheDao()))
    
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
                tagsToUpload.add(it.profileId)
            }
        }
        uploadPostWork(uriToStringArray(postImagesUri))
        clearAllAfterDonePosting()
    }
    
    private fun clearAllAfterDonePosting() {
        // clear all variables after inserting.
        hashTagList.clear()
        isHashTagListUpdated.postValue(true)
        locationTag = null
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
        val dataBuilder = Data.Builder()
            .putString(POST_TEXT_KEY, finalTextToUpload)
            .putLong(PROFILE_ID_KEY, profileId)
            .putStringArray(UPLOAD_IMAGE_PATH_KEY, uris)
            .putLongArray(POST_TAGS_KEY, tagsToUpload.toLongArray())
        
        locationTag?.let {
            dataBuilder.putString(PLACE_ID_KEY, locationTag!!.placeId)
            dataBuilder.putString(PLACE_PRIMARY, locationTag!!.primaryText)
            dataBuilder.putString(PLACE_SECONDARY, locationTag!!.secondaryText)
        }
        
        val mConstraints = Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val oneTimeWorkRequest = OneTimeWorkRequest
            .Builder(UploadPostPictures::class.java)
            .setConstraints(mConstraints)
            .setInputData(dataBuilder.build())
            .build()
        
        WorkManager.getInstance(app).enqueue(oneTimeWorkRequest)
        uuidWorkReq.postValue(oneTimeWorkRequest.id)
    }
    
    
    suspend fun getSearchResults(name: String) {
        val sharedPref = app.getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val ownID = sharedPref.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
        
        // get profileId, username, firstName, lastName
        val searchResFormDB = viewModelScope.async {
            db.searchDao().getSearchResult(name, ownID)
        }
        val finalResultWoPicUrl = searchResFormDB.await()
        
        // get images from firebase
        val imageUrlFromFirebase = viewModelScope.async {
            val tempList: MutableList<String> = mutableListOf()
            for (i in finalResultWoPicUrl) {
                val img = imageUtil.getProfilePictureUrl(i.profileId)
                tempList.add(img.toString())
            }
            tempList
        }
        
        val finalListWithImages = imageUrlFromFirebase.await()
        val finalListWithImage = mutableListOf<TagSearchResult>()
        for (i in finalResultWoPicUrl.indices) {
            val singleRes = TagSearchResult(
                finalResultWoPicUrl[i].profileId,
                finalResultWoPicUrl[i].firstName,
                finalResultWoPicUrl[i].lastName,
                finalResultWoPicUrl[i].username,
                finalListWithImages[i]
            )
            finalListWithImage.add(singleRes)
        }
        tagSearchResults.postValue(finalListWithImage)
    }
    
    suspend fun searchHashTag(hashTag: String) {
        hashTagList.apply {
            clear()
            addAll(db.hashtagDao().getHashTags(hashTag))
        }
        isHashTagListUpdated.postValue(true)
    }
}