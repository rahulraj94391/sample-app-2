package com.example.instagram.data.repo

import androidx.lifecycle.LiveData
import com.example.instagram.database.dao.CommentDao
import com.example.instagram.database.entity.Comment
import com.example.instagram.database.model.CommentLog
import com.example.instagram.domain.repo_contract.CommentRepo

class CommentRepoImpl(private val dao: CommentDao) :  CommentRepo{
    override fun commentCount(postId: Long): LiveData<Int> {
        return dao.commentCount(postId)
    }
    
    override suspend fun getAllComments(postId: Long, myProfileId: Long): MutableList<Comment> {
        return dao.getAllComments(postId, myProfileId)
    }
    
    override suspend fun getMyComment(postId: Long, myProfileId: Long): MutableList<Comment> {
        return dao.getMyComment(postId, myProfileId)
    }
    
    override suspend fun insertComment(comment: Comment): Long {
        return dao.insertComment(comment)
    }
    
    override suspend fun deleteCommentById(commentId: Long): Int {
        return dao.deleteCommentById(commentId)
    }
    
    override suspend fun getCommentLog(userId: Long): List<CommentLog> {
        return dao.getCommentLog(userId)
    }
}