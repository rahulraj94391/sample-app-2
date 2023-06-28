package com.example.instagram.database.dao

import androidx.room.Dao

@Dao
interface PostTextDao {
    fun insertTextByPostId()
    fun getTextByPostId()
    fun updateTextByPostId()
}