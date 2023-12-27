package com.example.instagram.domain.repo_contract

import androidx.lifecycle.LiveData
import com.example.instagram.data.entity.Comment
import com.example.instagram.screen_notification.model.CommentLog

interface CommentRepo {
    fun commentCount(postId: Long): LiveData<Int>
    suspend fun getAllComments(postId: Long, myProfileId: Long): MutableList<Comment>
    suspend fun getMyComment(postId: Long, myProfileId: Long): MutableList<Comment>
    suspend fun insertComment(comment: Comment): Long
    suspend fun deleteCommentById(commentId: Long): Int
    suspend fun getCommentLog(userId: Long): List<CommentLog>
}