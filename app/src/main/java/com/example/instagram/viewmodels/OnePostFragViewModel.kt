package com.example.instagram.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OnePostFragViewModel(app: Application) : AndroidViewModel(app) {
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var profileImageUrl = MutableLiveData<String>()
    val postImagesUrl = MutableLiveData<MutableList<String>>()

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


    suspend fun getProfilePicture(profileId: Long) {
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