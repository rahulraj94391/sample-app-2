package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.data.entity.Location

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location)
    
    @Delete
    suspend fun delete(location: Location): Int
    
    @Query("SELECT * FROM location WHERE placeId = :placeId")
    suspend fun getLocation(placeId: String): Location
    
}