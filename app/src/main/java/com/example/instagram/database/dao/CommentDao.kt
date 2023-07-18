package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.database.entity.Comment

@Dao
interface CommentDao {
    @Query("SELECT COUNT(post_id) FROM comment WHERE post_id = :postId")
    suspend fun commentCount(postId: Long): Int

    @Query("SELECT * FROM comment WHERE post_id = :postId AND commenter_id != :myProfileId ORDER BY comment_time DESC")
    suspend fun getAllComments(postId: Long, myProfileId: Long): MutableList<Comment>

    @Query("SELECT * FROM comment WHERE post_id = :postId AND commenter_id = :myProfileId ORDER BY comment_time DESC")
    suspend fun getMyComment(postId: Long, myProfileId: Long): MutableList<Comment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long

    @Query("DELETE FROM comment WHERE comment_id = :commentId")
    suspend fun deleteCommentById(commentId: Long): Int
}




