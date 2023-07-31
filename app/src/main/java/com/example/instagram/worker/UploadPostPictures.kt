package com.example.instagram.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Post
import com.example.instagram.database.entity.PostText
import com.example.instagram.database.entity.Tag
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

const val UPLOAD_IMAGE_PATH_KEY = "photo_uri"
const val PROFILE_ID_KEY = "profile_id"
const val POST_TEXT_KEY = "post_text"
const val POST_TAGS_KEY = "post_tagss"
private const val TAG = "CommTag_UploadPostPictures"

class UploadPostPictures(val context: Context, private val workerParameter: WorkerParameters) : CoroutineWorker(context, workerParameter) {
    private var imageUtil: ImageUtil = ImageUtil(context)
    private var db: AppDatabase = AppDatabase.getDatabase(context)
    private var storageRef: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "CHECK-POINT 1")
        
        //photo(s) Uri
        val originalUri = inputData.getStringArray(UPLOAD_IMAGE_PATH_KEY) ?: return Result.failure().also { }
        
        // post text should not be null
        val postText = inputData.getString(POST_TEXT_KEY) ?: return Result.failure()
        
        // profile id should not be -1
        val profileId = inputData.getLong(PROFILE_ID_KEY, -1)
        if (profileId == (-1).toLong()) return Result.failure()
        
        // tags could be null
        val tags = inputData.getLongArray(POST_TAGS_KEY)
        
        Log.d(TAG, "CHECK-POINT 2")
        
        val timeStamp = System.currentTimeMillis()
        val postId = db.postDao().insertPost(Post(profileId, timeStamp))
        db.postTextDao().insertPostText(PostText(postId, postText))
        Log.d(TAG, "CHECK-POINT 2a")
        val downscaleImageUris = imageUtil.getUriDownscaleImages(stringToUri(originalUri))
        Log.d(TAG, "CHECK-POINT 2b")
        uploadPostImages(postId, downscaleImageUris)
        Log.d(TAG, "CHECK-POINT 3")
        
        
        tags!!.let {
            Log.d(TAG, "inside the tags NOT NULL BLOCK")
            db.tagPeopleDao().insertPostTags(prepareTagsOnPost(tags, postId))
        }
        
        Log.d(TAG, "CHECK-POINT 4")
        return Result.success()
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
    
    private fun uploadPostImages(postId: Long, listOfImageUris: List<Uri>) {
        Log.d(TAG, "Uri Length = ${listOfImageUris.size}")
        var passCounter = 0
        var failCounter = 0
        for (i in listOfImageUris.indices) {
            val storageRef = storageRef.reference.child("${postId}_$i")
            val imageUri = listOfImageUris[i]
            imageUri.let { uri ->
                storageRef.putFile(uri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                            val map = HashMap<String, Any>()
                            map[postId.toString()] = uri2.toString()
                            map["serial"] = "${postId}_${i}"
                            
                            firebaseFirestore.collection("postImages").add(map).addOnCompleteListener { firestoreTask ->
                                if (firestoreTask.isSuccessful) {
                                    passCounter++
                                } else {
                                    failCounter++
                                }
                                
                                // attach placeholder image when unsuccessful while adding path to firestore(DB)
                                /*binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                                binding.progressBar.visibility = View.INVISIBLE*/
                                
                            }
                        }
                    } else { // when image upload to storage(drive like) fails
                        /*Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                        binding.progressBar.visibility = View.INVISIBLE*/
                    }
                }
            }
        }
        
        val message = if (failCounter > 0) {
            "$passCounter/${passCounter+failCounter} uploaded successfully"
        } else {
            "Photos uploaded successfully"
        }
        
        Log.d(TAG, "uploadPostImages: $message")
    }
}