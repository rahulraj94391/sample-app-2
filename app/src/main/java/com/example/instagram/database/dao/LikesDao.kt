package com.example.instagram.database.dao

import androidx.room.Dao

@Dao
interface LikesDao {
    fun getLikeCountByPostId()
    fun deleteLikesByPostId()
    fun getLikeListOfUsersByPostId()
}