package com.example.instagram.database.dao

import androidx.room.Dao

@Dao
interface FollowDao {
    fun getFollowers()
    fun getFollowing()
    fun insertConnection()
    fun deleteConnection()
}