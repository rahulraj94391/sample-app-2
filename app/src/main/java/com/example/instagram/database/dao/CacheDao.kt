package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.database.entity.ImageCache

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCacheUrl(imageCache: ImageCache): Long
    
    @Query("SELECT fileName FROM ImageCache where imageUrl = :url")
    suspend fun getCachedImageFileNameIfPresent(url: String): Long?
    
    @Query("SELECT imageUrl from ImageCache where imageUrl like '%'|| :postId || '_' || '%'")
    suspend fun getCachedPostImages(postId: Long): MutableList<String>
    
    @Query("Select imageUrl from ImageCache where imageUrl like '%'|| :profileId || '%'")
    suspend fun getCachedProfileImage(profileId: Long): String?
    
    @Query("update ImageCache set imageUrl = :newUrl where imageUrl is (Select imageUrl from ImageCache where imageUrl like '%'|| :profileId || '%')")
    suspend fun updateProfilePicImageCacheUrl(newUrl: String, profileId: Long): Int
    
    @Query("delete from ImageCache where imageUrl is (Select imageUrl from ImageCache where imageUrl like '%'|| :profileId || '%')")
    suspend fun deleteProfilePicImageCacheUrl(profileId: Long): Int
    
    @Query("select imageUrl from ImageCache where imageUrl like '%'|| :postId || '_0' || '%'")
    suspend fun getFirstImgFromEachPost(postId: Long): String?
}