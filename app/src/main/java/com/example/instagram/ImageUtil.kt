package com.example.instagram

import android.content.ContentResolver
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

private const val TAG = "ImageUtil"

class ImageUtil(val context: Context) {
    private val errorBitmap: Bitmap = ContextCompat.getDrawable(context, R.drawable.loading_error)!!.toBitmap()
    
    suspend fun getBitmap(url: String): Bitmap {
        Log.d(TAG, "getBitmap")
        val fileName = AppDatabase.getDatabase(context).cacheDao().getCachedImageFileNameIfPresent(url)
        return if (fileName == null) newURLFound(url)
        else getImageFromCache("$fileName") ?: onEntryPresentAndFileMissing(url, fileName)
    }
    
    private suspend fun onEntryPresentAndFileMissing(url: String, fileName: Long): Bitmap {
        Log.d(TAG, "onEntryPresentAndFileMissing")
        val downloadedBitmap = downloadBitmap(url) ?: return errorBitmap
        putImageInCache("$fileName", downloadedBitmap)
        return downloadedBitmap
    }
    
    private suspend fun newURLFound(url: String): Bitmap {
        Log.d(TAG, "newURLFound as - $url")
        val downloadedBitmap = downloadBitmap(url) ?: return errorBitmap
        val db = AppDatabase.getDatabase(context)
        val newRec = db.cacheDao().insertCacheUrl(ImageCache(url, System.currentTimeMillis()))
        val newFileName: String = newRec.toString()
        putImageInCache(newFileName, downloadedBitmap)
        return downloadedBitmap
    }
    
    private fun getImageFromCache(fileName: String): Bitmap? {
        Log.d(TAG, "getImageFromCache")
        val file = File(context.cacheDir, fileName)
        var bitmap: Bitmap? = null
        try {
            bitmap = BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e: Exception) {
            Log.d(TAG, "Exception while reading image file stream from cache.")
            e.message
        }
        return bitmap
    }
    
    private suspend fun putImageInCache(fileName: String, image: Bitmap) {
        Log.d(TAG, "putImageInCache")
        val file = File(context.cacheDir, fileName)
        try {
            val outputStream = withContext(Dispatchers.IO) {
                FileOutputStream(file)
            }
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            withContext(Dispatchers.IO) {
                outputStream.flush()
                outputStream.close()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Exception while putting image file into cache.")
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
    
    fun getBitmapFromUri(uri: Uri): Bitmap? {
        val contentResolver: ContentResolver = context.contentResolver
        return try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    fun resizePhoto(bitmap: Bitmap, H: Int = 377, W: Int = 720): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val aspRat = (w / h).toLong()
        //        val W = (aspRat * H).toInt()
        Log.d(TAG, "resizePhoto: height = $H, width = $W\nBitmap width = $w, height = $h\nAspect ratio = $aspRat")
        val b = Bitmap.createScaledBitmap(bitmap, W, H, false)
        return b
    }
    
    fun getUriDownscaleImages(postImagesUri: MutableList<Uri>): MutableList<Uri> {
        //        val finalList = mutableListOf<Uri>()
        //        postImagesUri.forEach {
        //            val bitmap = getBitmapFromUri(it)!!
        //            val downscaledBitmap = resizePhoto(bitmap)
        //            val tempFile: File = File(context.cacheDir, "${System.currentTimeMillis()}.jpeg")
        //            try {
        //                val fos = FileOutputStream(tempFile)
        //                downscaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        //                fos.close()
        //            } catch (e: IOException) {
        //                // Handle error
        //            }
        //            finalList.add(Uri.fromFile(tempFile))
        //        }
        ////        Log.d(TAG, "URI_TEST - final uris list from ImageUtil = $finalList")
        //        return finalList
        return postImagesUri
    }
}