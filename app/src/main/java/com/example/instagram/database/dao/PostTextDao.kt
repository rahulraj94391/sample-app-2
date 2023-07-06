package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.instagram.database.entity.PostText

@Dao
interface PostTextDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPostText(postText: PostText): Long
}