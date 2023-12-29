package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.data.entity.PostText

@Dao
interface PostTextDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPostText(postText: PostText): Long

    @Query("SELECT postText.postText FROM postText WHERE postId = :postId")
    suspend fun getPostText(postId: Long): String

}