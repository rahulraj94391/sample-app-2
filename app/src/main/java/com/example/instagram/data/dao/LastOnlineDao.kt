package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.instagram.data.entity.LastOnline

@Dao
interface LastOnlineDao {
    @Query("Select * from last_online where sender = :userId AND receiver = :myId")
    suspend fun getUserLastOnlineStatus(userId: Long, myId: Long): LastOnline?
    
    @Query("select * from last_online where sender = :myId AND receiver = :userId")
    suspend fun getMyLastOnlineTime(myId: Long, userId: Long): LastOnline?
    
    @Update
    suspend fun updateMyLastOnlineStatus(lastOnline: LastOnline): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyLastOnlineStatus(lastStatus: LastOnline): Long
    
    
}