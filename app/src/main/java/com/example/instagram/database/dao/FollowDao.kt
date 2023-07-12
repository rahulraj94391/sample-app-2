package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface FollowDao {
    @Query("SELECT COUNT(owner_id) FROM follow WHERE owner_id = :profileId")
    suspend fun getFollowerCount(profileId: Long): Int

    @Query("SELECT COUNT(follower_id) FROM follow WHERE follower_id = :profileId")
    suspend fun getFollowingCount(profileId: Long): Int

    @Query("SELECT COUNT(follower_id) FROM follow WHERE owner_id = :ownId AND follower_id = :userId")
    suspend fun isUserFollowingUser(ownId: Long, userId: Long): Int
}