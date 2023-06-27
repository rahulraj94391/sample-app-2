package com.example.instagram.database.dao

interface CommentDao {
    fun insertCommentByPostId()
    fun getAllCommentsByPostId()
    fun updateCommentByPostId()
    fun deleteCommentByPostId()
}




