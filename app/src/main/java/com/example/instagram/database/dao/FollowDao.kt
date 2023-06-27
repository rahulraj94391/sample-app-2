package com.example.instagram.database.dao

interface FollowDao {
    fun getFollowers()
    fun getFollowing()
    fun insertConnection()
    fun deleteConnection()
}