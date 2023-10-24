package com.example.instagram

import android.content.Context
import com.example.instagram.database.AppDatabase

private const val TAG = "ExportUserData_CommTag"

class ExportUserImages(private val profileId: Long, private val context: Context) {
    val filesToZip = listOf("path_to_file_1", "path_to_file_2")
    val outputZipPath = "path_to_output_zip.zip"
    
    val postIds = mutableListOf<Long>()
    val allImageLinks = mutableListOf<String>()
    val tempFileNames = mutableListOf<String>()
    val imageUtil = ImageUtil(context)
    
    
    suspend fun getAllPostIds() {
        val db = AppDatabase.getDatabase(context)
        val allPostIds = db.postDao().getAllPostOfProfile(profileId)
        postIds.addAll(allPostIds)
    }
    
    
    suspend fun getAllPostLinks() {
        for (imageId in postIds) {
            allImageLinks.addAll(imageUtil.getPostImages(imageId))
        }
        imageUtil.getProfilePictureUrl(profileId)?.let {
            allImageLinks.add(it)
        }
        
        postIds.clear()
    }
    
    
    
}