package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.data.entity.LocationCache

@Dao
interface LocationCacheDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: List<LocationCache>)
    
    @Query("Select * from locationCache where fullText LIKE '%' || :keyword || '%'")
    suspend fun getLocation(keyword: String): List<LocationCache>
    
}