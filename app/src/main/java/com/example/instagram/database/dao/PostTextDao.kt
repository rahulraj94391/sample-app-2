package com.example.instagram.database.dao

interface PostTextDao {
    fun insertTextByPostId()
    fun getTextByPostId()
    fun updateTextByPostId()
}