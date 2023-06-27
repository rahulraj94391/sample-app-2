package com.example.instagram.database.dao

interface LikesDao {
    fun getLikeCountByPostId()
    fun deleteLikesByPostId()
    fun getLikeListOfUsersByPostId()
}