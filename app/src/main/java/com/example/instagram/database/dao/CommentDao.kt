package com.example.instagram.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.database.entity.Comment
import com.example.instagram.database.model.CommentLog

@Dao
interface CommentDao {
    @Query("SELECT COUNT(post_id) FROM comment WHERE post_id = :postId")
    fun commentCount(postId: Long): LiveData<Int>
    
    @Query("SELECT * FROM comment WHERE post_id = :postId AND commenter_id != :myProfileId ORDER BY comment_time DESC")
    suspend fun getAllComments(postId: Long, myProfileId: Long): MutableList<Comment>
    
    @Query("SELECT * FROM comment WHERE post_id = :postId AND commenter_id = :myProfileId ORDER BY comment_time DESC")
    suspend fun getMyComment(postId: Long, myProfileId: Long): MutableList<Comment>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long
    
    @Query("DELETE FROM comment WHERE comment_id = :commentId")
    suspend fun deleteCommentById(commentId: Long): Int
    
    @Query("SELECT comment.comment_id, comment.commenter_id, login_credential.username, comment.post_id, comment.comment_time FROM comment LEFT JOIN login_credential ON comment.commenter_id = login_credential.profile_id WHERE comment.post_id IN (SELECT post.post_id FROM post WHERE post.profile_id = :ownerId) AND commenter_id != :ownerId ORDER BY comment.comment_time DESC")
    suspend fun getCommentLog(ownerId: Long): List<CommentLog>
}




