package com.example.instagram.database.dao

interface PostImageDao {
    fun insertImagesByPostId()
    fun getImagesByPostId()
}