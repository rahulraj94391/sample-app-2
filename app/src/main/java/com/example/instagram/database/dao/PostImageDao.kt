package com.example.instagram.database.dao

import androidx.room.Dao

@Dao
interface PostImageDao {
    fun insertImagesByPostId()
    fun getImagesByPostId()
}