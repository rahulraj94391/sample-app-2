package com.example.instagram

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.ImageCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL

private const val TAG = "ImageUtil"

class ImageUtil(val context: Context) {
    private val errorBitmap: Bitmap = ContextCompat.getDrawable(context, R.drawable.loading_error)!!.toBitmap()

    suspend fun getBitmap(url: String): Bitmap {
        val fileName = AppDatabase.getDatabase(context).cacheDao().getCachedImageFileNameIfPresent(url)
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
        val db = AppDatabase.getDatabase(context)
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
            Log.d(TAG, "Exception while reading image file stream from cache.")
            e.message
        }
        return bitmap
    }

    private suspend fun putImageInCache(fileName: String, image: Bitmap) {
        val file = File(context.cacheDir, fileName)
        try {
            val outputStream = FileOutputStream(file)
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

    private fun downloadBitmap(imageUrl: String): Bitmap? {
        return try {
            val conn = URL(imageUrl).openConnection()
            conn.connect()
            val inputStream = conn.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }



}