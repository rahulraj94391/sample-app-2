package com.example.instagram.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Post
import com.example.instagram.database.entity.PostText
import com.example.instagram.database.entity.Tag
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay

const val UPLOAD_IMAGE_PATH_KEY = "photo_uri"
const val PROFILE_ID_KEY = "profile_id"
const val POST_TEXT_KEY = "post_text"
const val POST_TAGS_KEY = "post_tagss"
const val IS_UPLOAD_FINISHED = "is_upload_finished"

private const val TAG = "UploadPostPictures_CommTag"

class UploadPostPictures(val context: Context, private val workerParameter: WorkerParameters) : CoroutineWorker(context, workerParameter) {
    private var imageUtil: ImageUtil = ImageUtil(context)
    private var db: AppDatabase = AppDatabase.getDatabase(context)
    private var storageRef: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    var total = 0
    var success = 0
    var fail = 0
    
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "CHECK-POINT 1")
        
        //photo(s) Uri
        val originalUri = inputData.getStringArray(UPLOAD_IMAGE_PATH_KEY) ?: return Result.failure()
        
        // post text should not be null
        val postText = inputData.getString(POST_TEXT_KEY) ?: return Result.failure()
        
        // profile id should not be -1
        val profileId = inputData.getLong(PROFILE_ID_KEY, -1)
        if (profileId == (-1).toLong()) return Result.failure()
        
        // tags could be null
        val tags = inputData.getLongArray(POST_TAGS_KEY)
        
        Log.d(TAG, "CHECK-POINT 2")
        total = originalUri.size
        
        val timeStamp = System.currentTimeMillis()
        val postId = db.postDao().insertPost(Post(profileId, timeStamp))
        db.postTextDao().insertPostText(PostText(postId, postText))
        Log.d(TAG, "CHECK-POINT 3")
        val downscaleImageUris = imageUtil.getUriDownscaleImages(stringToUri(originalUri))
        Log.d(TAG, "CHECK-POINT 4")
        uploadPostImages(postId, downscaleImageUris)
        Log.d(TAG, "CHECK-POINT 5")
        
        tags!!.let {
            Log.d(TAG, "inside the tags NOT NULL BLOCK")
            db.tagPeopleDao().insertPostTags(prepareTagsOnPost(tags, postId))
        }
        
        Log.d(TAG, "CHECK-POINT 6")
        delay(1000)
        return Result.success(Data.Builder().putBoolean(IS_UPLOAD_FINISHED, true).build())
    }
    
    private fun prepareTagsOnPost(tags: LongArray, postId: Long): MutableList<Tag> {
        val list = mutableListOf<Tag>()
        for (i in tags) {
            list.add(Tag(postId, i))
        }
        Log.d(TAG, "prepareTagsOnPost: $list")
        return list
    }
    
    private fun stringToUri(uriString: Array<String>): MutableList<Uri> {
        val list = mutableListOf<Uri>()
        for (uri in uriString) {
            list.add(Uri.parse(uri))
        }
        return list
    }
    
    private suspend fun uploadPostImages(postId: Long, listOfImageUris: List<Uri>) {
        for (i in listOfImageUris.indices) {
            val storageRef = storageRef.reference.child("${postId}_$i")
            val imageUri = listOfImageUris[i]
            imageUri.let { uri ->
                storageRef.putFile(uri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "upload to storage complete.")
                        storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                            val map = HashMap<String, Any>()
                            map[postId.toString()] = uri2.toString()
                            map["serial"] = "${postId}_${i}"
                            firebaseFirestore.collection("postImages").add(map).addOnCompleteListener {
                                Log.d(TAG, "----")
                                if (it.isSuccessful) {
                                    success++
                                } else {
                                    fail++
                                }
                                Log.d(TAG, "upload to db completed")
                                setProgressAsync(Data.Builder().putInt("", 2).build())
                            }
                        }
                    }
                }
            }
        }
    }
}