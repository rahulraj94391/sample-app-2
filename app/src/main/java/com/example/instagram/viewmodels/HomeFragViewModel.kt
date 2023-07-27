package com.example.instagram.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.DateTime
import com.example.instagram.TimeFormatting
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Likes
import com.example.instagram.database.entity.SavedPost
import com.example.instagram.database.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "CommTag_HomeFragViewModel"

class HomeFragViewModel(private val currentProfile: Long, private val db: AppDatabase) :
    ViewModel() {
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val postsToShow = MutableLiveData<MutableList<Post>>()
    private val postIdsAlreadyShown = mutableSetOf<Long>()

    fun addNewPostToList(loggedInProfileId: Long) {
        viewModelScope.launch {
            val tempList: MutableList<Post> = mutableListOf()
            val postsToShowOnHome = db.postDao().getPostOfFollowers(loggedInProfileId)

            for (i in postsToShowOnHome) {
                tempList.add(getPost(i))
            }
            postsToShow.postValue(tempList)
        }
    }

    private suspend fun getPost(postId: Long): Post {
        val profileId = viewModelScope.async { getProfileId(postId) }
        val profImageUrl = viewModelScope.async { getProfilePicture(profileId.await()) }
        val profileUsername = viewModelScope.async { getProfileUserName(postId) }
        val listOfPostPhotos = viewModelScope.async { getPostImages(postId) }
        val isPostAlreadyLiked = viewModelScope.async { getPostLikeStat(postId, currentProfile) }
        val isPostAlreadySaved = viewModelScope.async { getPostSaveStat(postId, currentProfile) }
        val likeCount = viewModelScope.async { getFormattedLikeCount(postId) }
        val postDesc = viewModelScope.async { getPostDesc(postId) }
        val commentCount = viewModelScope.async { getFormattedCommentCount(postId) }
        val postTime = viewModelScope.async { getFormattedTimeOfPost(postId) }

        val post = Post(
            postId = postId,
            profileId = profileId.await(),
            profileImageUrl = profImageUrl.await(),
            profileUsername = profileUsername.await(),
            listOfPostPhotos = listOfPostPhotos.await(),
            isPostAlreadyLiked = isPostAlreadyLiked.await(),
            isPostAlreadySaved = isPostAlreadySaved.await(),
            likeCount = likeCount.await(),
            postDesc = postDesc.await(),
            commentCount = commentCount.await(),
            timeOfPost = postTime.await()
        )

//        Log.d(TAG, "Post generated = $post")
        return post
    }

    fun likePost(postId: Long, profileId: Long) {
        viewModelScope.launch {
            db.likesDao().insertNewLike(Likes(postId, profileId, System.currentTimeMillis()))
        }
    }

    fun removeLike(postId: Long, profileId: Long) {
        viewModelScope.launch {
            db.likesDao().deleteLike(profileId, postId)
        }
    }

    fun savePost(profileId: Long, postId: Long) {
        viewModelScope.launch {
            db.savedPostDao().savePost(SavedPost(profileId, postId, System.currentTimeMillis()))
        }
    }

    fun removeSavedPost(profileId: Long, postId: Long) {
        viewModelScope.launch {
            db.savedPostDao().deleteSavedPost(postId, profileId)
        }
    }

    private suspend fun getFormattedTimeOfPost(postId: Long): String {
        val time = db.postDao().getPostTime(postId)
        return DateTime.timeFormatter(time, TimeFormatting.POST)
    }

    private suspend fun getFormattedCommentCount(postId: Long): String {
        return when (val cc = db.commentDao().commentCount(postId)) {
            0 -> "0 comment"
            1 -> "View 1 comment"
            else -> "View all $cc comments"
        }
    }

    suspend fun getFormattedLikeCount(postId: Long): String {
        return "${db.likesDao().likeCount(postId)} like"
    }

    private suspend fun getPostDesc(postId: Long): String {
        return db.postTextDao().getPostText(postId)
    }

    private suspend fun getProfileUserName(postId: Long): String {
        return db.postDao().getUsername(postId)
    }

    private suspend fun getPostLikeStat(postId: Long, profileId: Long): Boolean {
        return db.likesDao().isPostLikedByProfile(postId, profileId) > 0
    }

    private suspend fun getPostSaveStat(postId: Long, profileId: Long): Boolean {
        return db.savedPostDao().isPostSavedByProfile(profileId, postId) > 0
    }

    private suspend fun getProfileId(postId: Long): Long {
        return db.postDao().getProfileId(postId)
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

    private suspend fun getPostImages(postId: Long): MutableList<String> {
        val imgURLList = mutableListOf<String>()
        val snapShot =
            firebaseFireStore
                .collection("postImages")
                .whereIn(
                    "serial",
                    mutableListOf(
                        "${postId}_0",
                        "${postId}_1",
                        "${postId}_2",
                        "${postId}_3",
                        "${postId}_4",
                        "${postId}_5"
                    )
                )
                .get()
                .await()
        for (i in snapShot) {
            imgURLList.add(i.data["$postId"].toString())
        }
        return imgURLList
    }
}