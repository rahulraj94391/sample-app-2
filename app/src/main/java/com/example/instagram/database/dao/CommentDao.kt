package com.example.instagram.database.dao

import androidx.room.Dao

@Dao
interface CommentDao {
    fun insertCommentByPostId()
    fun getAllCommentsByPostId()
    fun updateCommentByPostId()
    fun deleteCommentByPostId()
}




