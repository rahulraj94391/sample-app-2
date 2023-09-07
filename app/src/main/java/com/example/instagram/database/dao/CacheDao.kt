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
    
}