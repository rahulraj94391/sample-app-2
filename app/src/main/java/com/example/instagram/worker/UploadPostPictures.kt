package com.example.instagram.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.instagram.ImageUtil
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.ImageCache
import com.example.instagram.database.entity.Location
import com.example.instagram.database.entity.Post
import com.example.instagram.database.entity.PostText
import com.example.instagram.database.entity.Tag
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val UPLOAD_IMAGE_PATH_KEY = "photo_uri"
const val PROFILE_ID_KEY = "profile_id"
const val POST_TEXT_KEY = "post_text"
const val POST_TAGS_KEY = "post_tagss"
const val IS_UPLOAD_FINISHED = "is_upload_finished"
const val PLACE_ID_KEY = "place_id"
const val PLACE_PRIMARY = "place_primary"
const val PLACE_SECONDARY = "place_secondary"

private const val TAG = "UploadPostPictures_CommTag"

class UploadPostPictures(val context: Context, private val workerParameter: WorkerParameters) : CoroutineWorker(context, workerParameter) {
    private var imageUtil: ImageUtil = ImageUtil(context)
    private var db: AppDatabase = AppDatabase.getDatabase(context)
    private var storageRef: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    private var total = 0
    private var success = 0
    private var fail = 0
    
    
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
        
        // get the location (if tagged)
        var location: Location? = null
        val placeId = inputData.getString(PLACE_ID_KEY)
        
        placeId?.let {
            val primaryText = inputData.getString(PLACE_PRIMARY)
            val secondaryText = inputData.getString(PLACE_SECONDARY)
            location = Location(it, primaryText, secondaryText)
        }
        
        location?.let {
            db.locationDao().insert(it)
            Log.d(TAG, "location received as = $location")
        }
        
        
        Log.d(TAG, "CHECK-POINT 2")
        total = originalUri.size
        
        val postId = db.postDao().insertPost(Post(profileId, System.currentTimeMillis(), location?.placeId))
        db.postTextDao().insertPostText(PostText(postId, postText))
        Log.d(TAG, "CHECK-POINT 3")
        val downscaleImageUris = imageUtil.getUriDownscaleImages(stringToUri(originalUri))
        Log.d(TAG, "CHECK-POINT 4")
        uploadPostImages(postId, downscaleImageUris, imageUtil)
        Log.d(TAG, "CHECK-POINT 5")
        
        tags!!.let {
            Log.d(TAG, "inside the tags NOT NULL BLOCK")
            db.tagPeopleDao().insertPostTags(prepareTagsOnPost(tags, postId))
        }
        
        Log.d(TAG, "CHECK-POINT 6")
        return Result.success(Data.Builder().putBoolean(IS_UPLOAD_FINISHED, true).build())
    }
    
    private suspend fun insertTagsInDB(str: String, postId: Long) {
        val tags = extractKeywords(str)
        
        
    }
    
    fun parseHashTags(str: String): MutableList<String> {
        val s = str.lowercase()
        val tagLists: MutableSet<String> = mutableSetOf()
        val sb = StringBuilder()
        var flag = false
        for (c in s) {
            if (c == '#' && flag && sb.isNotEmpty()) {
                tagLists.add(sb.toString())
                sb.clear()
            }
            if (c == '#') {
                flag = true
                continue
            } else if (!flag) {
                continue
            } else if (isValidChar(c)) {
                sb.append(c)
            } else {
                flag = false
                tagLists.add(sb.toString())
                sb.clear()
            }
        }
        if (sb.isNotEmpty()) {
            tagLists.add(sb.toString())
        }
        return tagLists.toMutableList()
    }
    
    
    private fun isValidChar(c: Char): Boolean {
        val c1 = c in '0'..'9'
        val c2 = c in 'a'..'z'
        val c3 = c in 'A'..'Z'
        val c4 = c == '_'
        return c1 || c2 || c3 || c4
    }
    
    private fun extractKeywords(inputText: String): List<String> {
        val keywords = mutableSetOf<String>()
        
        // Regular expression to match hashtags and extract keywords
        val regex = Regex("#(\\w+)")
        
        // Find all matches in the input text
        val matches = regex.findAll(inputText)
        
        // Extract keywords from matches and add to the list
        for (match in matches) {
            val keyword = match.groupValues[1]
            keywords.add(keyword)
        }
        
        return keywords.toList()
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
    
    private fun uploadPostImages(postId: Long, listOfImageUris: List<Uri>, imageUtil: ImageUtil) {
        for (i in listOfImageUris.indices) {
            val storageRef = storageRef.reference.child("${postId}_$i")
            val imageUri = listOfImageUris[i]
            imageUri.let { uri ->
                storageRef.putFile(uri).addOnCompleteListener { task ->
                    
                    if (task.isSuccessful) {
                        Log.d(TAG, "upload to storage complete.")
                        storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                            Log.d(TAG, "download Url = $uri2")
                            CoroutineScope(Dispatchers.IO).launch {
                                val cache = ImageCache(uri2.toString(), System.currentTimeMillis())
                                val rowsEntered = db.cacheDao().insertCacheUrl(cache)
                                Log.d(TAG, "cache image = $cache")
                                Log.d(TAG, "rowsEntered = $rowsEntered")
                            }
                            
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
        imageUtil.clearTempFiles()
    }
}