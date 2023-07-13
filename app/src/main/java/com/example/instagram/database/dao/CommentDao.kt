package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CommentDao {
    @Query("SELECT COUNT(post_id) FROM comment WHERE post_id = :postId")
    suspend fun commentCount(postId: Long): Int


}




