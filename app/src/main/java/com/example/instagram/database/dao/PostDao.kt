package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.instagram.database.entity.Post

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPost(post: Post): Long



}