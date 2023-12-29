package com.example.instagram.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.data.entity.Comment
import com.example.instagram.screen_notification.model.CommentLog

@Dao
interface CommentDao {
    @Query("SELECT COUNT(postId) FROM comment WHERE postId = :postId")
    fun commentCount(postId: Long): LiveData<Int>
    
    @Query("SELECT * FROM comment WHERE postId = :postId AND commenterId != :myProfileId ORDER BY commentTime DESC")
    suspend fun getAllComments(postId: Long, myProfileId: Long): MutableList<Comment>
    
    @Query("SELECT * FROM comment WHERE postId = :postId AND commenterId = :myProfileId ORDER BY commentTime DESC")
    suspend fun getMyComment(postId: Long, myProfileId: Long): MutableList<Comment>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long
    
    @Query("DELETE FROM comment WHERE commentId = :commentId")
    suspend fun deleteCommentById(commentId: Long): Int
    
    @Query("SELECT comment.commentId, comment.commenterId, login_credential.username, comment.postId, comment.commentTime FROM comment LEFT JOIN login_credential ON comment.commenterId = login_credential.profileId WHERE comment.postId IN (SELECT post.postId FROM post WHERE post.profileId = :ownerId) AND commenterId != :ownerId ORDER BY comment.commentTime DESC")
    suspend fun getCommentLog(ownerId: Long): List<CommentLog>
}




