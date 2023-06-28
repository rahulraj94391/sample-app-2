package com.example.instagram.database.dao

import androidx.room.Dao

@Dao
interface PostDao {
    fun getPostById()
    fun insertPost()
    fun deletePost()
}