package com.example.instagram.data.dao

import androidx.room.Dao

@Dao
interface PostImageDao {
    fun insertImagesByPostId()
    fun getImagesByPostId()
}