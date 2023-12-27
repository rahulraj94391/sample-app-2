package com.example.instagram.common.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.instagram.R
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.entity.ImageCache
import com.example.instagram.data.common_model.PostPreview
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

private const val TAG = "ImageUtil"

class ImageUtil(val context: Context) {
    private val tempImageFiles = mutableListOf<String>()
    private val db = AppDatabase.getDatabase(context)
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val errorBitmap: Bitmap = ContextCompat.getDrawable(context, R.drawable.person_24)!!.toBitmap()
    
    suspend fun getBitmap(url: String): Bitmap {
        Log.d(TAG, "getBitmap:")
        val fileName = db.cacheDao().getCachedImageFileNameIfPresent(url)
        Log.d(TAG, "filename = $fileName")
        return if (fileName == null) newURLFound(url)
        else getImageFromCache("$fileName") ?: onEntryPresentAndFileMissing(url, fileName)
    }
    
    /*suspend fun getBitmapPath() {
        // val fileName = db.cacheDao().getCachedImageFileNameIfPresent(url)
        
    }
    
    fun addTempFileNameToList(fileName: String) = tempImageFiles.add(fileName)*/
    
    private suspend fun onEntryPresentAndFileMissing(url: String, fileName: Long): Bitmap {
        Log.d(TAG, "onEntryPresentAndFileMissing")
        val downloadedBitmap = downloadBitmap(url) ?: return errorBitmap
        putImageInCache("$fileName", downloadedBitmap)
        return downloadedBitmap
    }
    
    private suspend fun newURLFound(url: String): Bitmap {
        Log.d(TAG, "newURLFound:")
        val downloadedBitmap = downloadBitmap(url) ?: return errorBitmap
        val newRec = db.cacheDao().insertCacheUrl(ImageCache(url, System.currentTimeMillis()))
        val newFileName: String = newRec.toString()
        putImageInCache(newFileName, downloadedBitmap)
        return downloadedBitmap
    }
    
    private fun getImageFromCache(fileName: String): Bitmap? {
        Log.d(TAG, "getImageFromCache: ")
        val file = File(context.cacheDir, "$fileName.jpeg")
        var bitmap: Bitmap? = null
        try {
            bitmap = BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e: Exception) {
            e.message
        }
        return bitmap
    }
    
    private suspend fun putImageInCache(fileName: String, image: Bitmap) {
        Log.d(TAG, "putImageInCache:")
        val file = File(context.cacheDir, "$fileName.jpeg")
        try {
            val outputStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, 20, outputStream)
            withContext(Dispatchers.IO) {
                outputStream.flush()
                outputStream.close()
            }
        } catch (e: Exception) {
            e.message
        }
    }
    
    suspend fun downloadBitmap(imageUrl: String): Bitmap? {
        Log.d(TAG, "downloadBitmap:")
        val photo = CoroutineScope(Dispatchers.IO).async {
            try {
                val conn = withContext(Dispatchers.IO) {
                    URL(imageUrl).openConnection()
                }
                withContext(Dispatchers.IO) {
                    conn.connect()
                }
                val inputStream = withContext(Dispatchers.IO) {
                    conn.getInputStream()
                }
                val bitmap = BitmapFactory.decodeStream(inputStream)
                withContext(Dispatchers.IO) {
                    inputStream.close()
                }
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        return photo.await()
    }
    
    suspend fun getBitmapFromUri(uri: Uri, desiredRatio: Double = 0.8, desiredHeight: Double = 1080.0): Bitmap? {
        Log.d(TAG, "getBitmapFromUri:")
        return try {
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            downScale(bitmap, desiredRatio, desiredHeight)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    private fun downScale(bitmap: Bitmap?, desiredRatio: Double, desiredHeight: Double): Bitmap? {
        Log.d(TAG, "downScale:")
        if (bitmap == null) return null
        val imageViewRatio = 4.toDouble() / 5.toDouble()
        var w: Double
        var h: Double
        var newHeight = desiredHeight
        var newWidth = desiredRatio * newHeight
        
        bitmap.apply {
            w = this.width.toDouble()
            h = this.height.toDouble()
        }
        val ratio = w / h
        if (h <= newHeight && w <= newWidth) return bitmap
        if (ratio < imageViewRatio) {
            newWidth = ratio * newHeight
        } else if (ratio > imageViewRatio) {
            newHeight = newWidth / ratio
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth.toInt(), newHeight.toInt(), true)
    }
    
    suspend fun getUriDownscaleImages(postImagesUri: MutableList<Uri>, desiredRatio: Double = 0.8, desiredHeight: Double = 1080.0): MutableList<Uri> {
        Log.d(TAG, "getUriDownscaleImages:")
        val finalList = mutableListOf<Uri>()
        postImagesUri.forEach {
            val downscaledBitmap = getBitmapFromUri(it, desiredRatio, desiredHeight)!!
            val tempFileName = "${System.currentTimeMillis()}.jpeg"
            tempImageFiles.add(tempFileName)
            Log.d(TAG, "temp file names in getUriDownscaleImages method = $tempFileName")
            val tempFile = File(context.cacheDir, tempFileName)
            try {
                val fos = FileOutputStream(tempFile)
                downscaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                fos.close()
            } catch (e: IOException) {
                // Handle error
            }
            finalList.add(Uri.fromFile(tempFile))
        }
        Log.d(TAG, "URI_TEST - final uris list from ImageUtil = $finalList")
        return finalList
        // return postImagesUri
    }
    
    /*---------------------------------------------—---------------------------------------------—---------------------------------------------—---------------------------------------------—*/
    
    
    suspend fun getProfilePictureUrl(profileId: Long, docId: MutableList<String> = mutableListOf()): String? {
        Log.d(TAG, "getProfilePictureUrl:")
        var profileImageUrl: String? = null
        val snapShot = firebaseFireStore
            .collection("profileImages")
            .whereEqualTo("ppid", "$profileId")
            .get()
            .await()
        for (i in snapShot) {
            profileImageUrl = i.data["$profileId"].toString()
            docId.add(i.reference.id)
            break
        }
        return profileImageUrl
    }
    
    suspend fun getPostImages(postId: Long): MutableList<String> {
        Log.d(TAG, "getPostImages:")
        val imgURLList = mutableListOf<String>()
        val snapShot = firebaseFireStore
            .collection("postImages")
            .whereIn("serial", mutableListOf("${postId}_0", "${postId}_1", "${postId}_2", "${postId}_3", "${postId}_4", "${postId}_5"))
            .get()
            .await()
        for (i in snapShot) {
            imgURLList.add(i.data["$postId"].toString())
        }
        return imgURLList
    }
    
    suspend fun getProfilePictureByPostId(postId: Long): String? {
        Log.d(TAG, "getProfilePictureByPostId:")
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
        return profPic
    }
    
    suspend fun getOneImagePerPost(postIds: MutableList<Long>): MutableList<PostPreview> {
        val imgURLList = mutableListOf<PostPreview>()
        for (postId in postIds) {
            val snapShots = firebaseFireStore
                .collection("postImages")
                .whereEqualTo("serial", "${postId}_0")
                .get()
                .await()
            for (i in snapShots) {
                val link = i.data["$postId"].toString()
                imgURLList.add(PostPreview(postId, link))
            }
        }
        Log.d(TAG, "getOneImagePerPost:$imgURLList")
        return imgURLList
    }
    
    fun clearTempFiles() {
        for (fileName in tempImageFiles) {
            val fileToDelete = File(context.cacheDir, fileName)
            val deleted = fileToDelete.delete()
            Log.d(TAG, "is temp file($fileName) deleted? = $deleted")
        }
        tempImageFiles.clear()
    }
    
    /*fun uploadProfileImage(imageUriToUpload: Uri, docId: String = "", loggedInProfileId: Long) {
        imageUriToUpload.let { uri ->
            val storageRef = storageRef.reference.child("$loggedInProfileId")
            storageRef.putFile(uri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                        val map = HashMap<String, Any>()
                        map[loggedInProfileId.toString()] = uri2.toString()
                        map["ppid"] = loggedInProfileId.toString()
                        
                        if (docId != "") {
                            updateProfilePictureEntryOnFireStore(docId, map)
                        } else {
                            uploadNewProfilePicture(map)
                        }
                    }
                }
            }
        }
    }
    
    private fun updateProfilePictureEntryOnFireStore(docId: String, map: HashMap<String, Any>): Boolean {
        firebaseFireStore.collection("profileImages").document(docId).set(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "Profile pic updated successfully.")
            } else {
                Log.d(TAG, "Profile pic updating failed.")
            }
        }
        return true
    }
    
    private fun uploadNewProfilePicture(map: HashMap<String, Any>): Boolean {
        firebaseFireStore.collection("profileImages").add(map).addOnCompleteListener { firestoreTask ->
            if (firestoreTask.isSuccessful) {
                Log.d(TAG, "uploadNewProfilePicture: Profile picture uploaded.")
            } else {
                Log.d(TAG, "uploadNewProfilePicture: Profile pic uploading failed.")
            }
        }
        return true
    }*/
    
    
    /*fun uploadProfileImageEEE(profileId: Long, profilePicUri: Uri?) {
        profilePicUri?.let { uri ->
            val storageRef = storageRef.reference.child("$profileId")
            storageRef.putFile(uri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                        val map = HashMap<String, Any>()
                        map[profileId.toString()] = uri2.toString()
                        map["ppid"] = profileId.toString()
                        firebaseFireStore.collection("profileImages").add(map).addOnCompleteListener { firestoreTask ->
                            //                            if (firestoreTask.isSuccessful) {
                            //                                Toast.makeText(app, "Uploaded successfully", Toast.LENGTH_SHORT).show()
                            //                            }
                            //                            else {
                            //                                Toast.makeText(app, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
                            //                            }
                            
                            // attach placeholder image when unsuccessful while adding path to firestore(DB)
                            //                            binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                            //                            binding.progressBar.visibility = View.INVISIBLE
                            
                        }
                    }
                } else {
                    // when image upload to storage(drive like) fails
                    //                    Toast.makeText(app, task.exception?.message, Toast.LENGTH_SHORT).show()
                    //                    binding.imageView.setImageResource(R.drawable.ic_launcher_background)
                    //                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }*/
}