package com.example.instagram

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.ImageCache
import com.example.instagram.database.model.OnePhotoPerPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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

private const val TAG = "ImageUtil_CommTag"

class ImageUtil(val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private var storageRef: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val errorBitmap: Bitmap = ContextCompat.getDrawable(context, R.drawable.loading_error)!!.toBitmap()
    
    suspend fun getBitmap(url: String): Bitmap {
        val fileName = db.cacheDao().getCachedImageFileNameIfPresent(url)
        return if (fileName == null) newURLFound(url)
        else getImageFromCache("$fileName") ?: onEntryPresentAndFileMissing(url, fileName)
    }
    
    private suspend fun onEntryPresentAndFileMissing(url: String, fileName: Long): Bitmap {
        val downloadedBitmap = downloadBitmap(url) ?: return errorBitmap
        putImageInCache("$fileName", downloadedBitmap)
        return downloadedBitmap
    }
    
    private suspend fun newURLFound(url: String): Bitmap {
        val downloadedBitmap = downloadBitmap(url) ?: return errorBitmap
        val newRec = db.cacheDao().insertCacheUrl(ImageCache(url, System.currentTimeMillis()))
        val newFileName: String = newRec.toString()
        putImageInCache(newFileName, downloadedBitmap)
        return downloadedBitmap
    }
    
    private fun getImageFromCache(fileName: String): Bitmap? {
        val file = File(context.cacheDir, fileName)
        var bitmap: Bitmap? = null
        try {
            bitmap = BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e: Exception) {
            e.message
        }
        return bitmap
    }
    
    private suspend fun putImageInCache(fileName: String, image: Bitmap) {
        val file = File(context.cacheDir, fileName)
        try {
            val outputStream = withContext(Dispatchers.IO) {
                FileOutputStream(file)
            }
            image.compress(Bitmap.CompressFormat.JPEG, 20, outputStream)
            withContext(Dispatchers.IO) {
                outputStream.flush()
                outputStream.close()
            }
        } catch (e: Exception) {
            e.message
        }
    }
    
    private suspend fun downloadBitmap(imageUrl: String): Bitmap? {
        val photo = CoroutineScope(Dispatchers.IO).async {
            try {
                val conn = URL(imageUrl).openConnection()
                conn.connect()
                val inputStream = conn.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        return photo.await()
    }
    
    fun getBitmapFromUri(uri: Uri, desiredRatio: Double = 0.8, desiredHeight: Double = 1080.0): Bitmap? {
        return try {
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            downScale(bitmap, desiredRatio, desiredHeight)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    private fun downScale(bitmap: Bitmap?, desiredRatio: Double, desiredHeight: Double): Bitmap? {
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
    
    fun getUriDownscaleImages(postImagesUri: MutableList<Uri>, desiredRatio: Double = 0.8, desiredHeight: Double = 1080.0): MutableList<Uri> {
        val finalList = mutableListOf<Uri>()
        postImagesUri.forEach {
            val downscaledBitmap = getBitmapFromUri(it, desiredRatio, desiredHeight)!!
            val tempFile = File(context.cacheDir, "${System.currentTimeMillis()}.jpeg")
            try {
                val fos = FileOutputStream(tempFile)
                downscaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
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
    
    suspend fun getOneImagePerPost(postIds: MutableList<Long>): MutableList<OnePhotoPerPost> {
        val imgURLList = mutableListOf<OnePhotoPerPost>()
        for (postId in postIds) {
            val snapShots = firebaseFireStore
                .collection("postImages")
                .whereEqualTo("serial", "${postId}_0")
                .get()
                .await()
            for (i in snapShots) {
                val link = i.data["$postId"].toString()
                imgURLList.add(OnePhotoPerPost(postId, link))
            }
        }
        return imgURLList
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