package com.example.instagram.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.OnePost
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "CommTag_OnePostFragViewModel"

class OnePostFragViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getDatabase(app)
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var profileImageUrl = MutableLiveData<String>()
    val postImagesUrl = MutableLiveData<MutableList<String>>()
    val likeCount = MutableLiveData<Int>(0)
    val commentCount = MutableLiveData<Int>(0)

    private suspend fun getProfileImage(profileId: Long): MutableList<String> {
        val imgURLList = mutableListOf<String>()
        val snapShot = firebaseFireStore.collection("profileImages").get().await()
        for (i in snapShot) {
            imgURLList.add(i.data["$profileId"].toString())
        }
        return imgURLList
    }


    suspend fun getPostImages(postId: Long) {
        val imgURLList = mutableListOf<String>()
        val snapShot =
            firebaseFireStore
                .collection("postImages")
                .whereIn("serial", mutableListOf("${postId}_0", "${postId}_1", "${postId}_2", "${postId}_3", "${postId}_4", "${postId}_5"))
                .get()
                .await()
        for (i in snapShot) {
            imgURLList.add(i.data["$postId"].toString())
        }
        postImagesUrl.postValue(imgURLList)
    }

    suspend fun getLikeCount(postId: Long) {
        likeCount.postValue(db.likesDao().getLikeCountOnPost(postId))
    }

    suspend fun getCommentCount(postId: Long) {
        commentCount.postValue(db.commentDao().commentCount(postId))
    }

    suspend fun getPostDetails(postId: Long, currentProfileId: Long): OnePost {
        val liked = db.likesDao().isPostLikedByProfile(postId, currentProfileId)
        val saved = db.savedPostDao().isPostSavedByProfile(currentProfileId, postId)
        val profileName = db.postDao().getUsername(postId)
        val postTime = db.postDao().getPostTime(postId)
        val postText = db.postTextDao().getPostText(postId)

        var likeStat = false
        if (liked > 0) likeStat = true

        var saveStat = false
        if (saved > 0) saveStat = true

        Log.d(TAG, "post text = $postText")

        return OnePost(
            likeStat,
            saveStat,
            profileName,
            postText,
            postTime
        )
    }

    suspend fun getProfilePictureByPostId(postId: Long) {
        val profileId = db.postDao().getProfileId(postId)
        var profPic: String? = null
        val snapShot = firebaseFireStore
            .collection("profileImages")
            .whereEqualTo("ppid", "$profileId")
            .get()
            .await()
        for (i in snapShot) {
            profPic = i.data["$profileId"].toString()
            break
        }
        this.profileImageUrl.postValue(profPic)
    }
}