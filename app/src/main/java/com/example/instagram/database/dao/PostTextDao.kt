package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.database.entity.PostText

@Dao
interface PostTextDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPostText(postText: PostText): Long

    @Query("SELECT post_text.post_text FROM post_text WHERE post_id = :postId")
    suspend fun getPostText(postId: Long): String

}