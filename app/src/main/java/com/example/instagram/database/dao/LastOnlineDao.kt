package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.instagram.database.entity.LastOnline

@Dao
interface LastOnlineDao {
    @Query("Select * from last_online where sender = :userId AND receiver = :myId")
    suspend fun getUserLastOnlineStatus(userId: Long, myId: Long): LastOnline?
    
    @Query("select * from last_online where sender = :myId AND receiver = :userId")
    suspend fun getMyLastOnlineStatus(myId: Long, userId: Long): LastOnline?
    
//    @Query("UPDATE last_online SET time = :time where sender = :myId and receiver = :userId")
    @Update
    suspend fun updateMyLastOnlineStatus(lastOnline: LastOnline): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyLastOnlineStatus(lastStatus: LastOnline): Long
    
    
}