package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.database.entity.Follow

@Dao
interface FollowDao {
    @Query("SELECT COUNT(follower_id) FROM follow WHERE follower_id = :profileId")
    suspend fun getFollowerCount(profileId: Long): Int

    @Query("SELECT COUNT(owner_id) FROM follow WHERE owner_id = :profileId")
    suspend fun getFollowingCount(profileId: Long): Int

    @Query("SELECT COUNT(follower_id) FROM follow WHERE owner_id = :ownId AND follower_id = :userId")
    suspend fun isUserFollowingUser(ownId: Long, userId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewFollow(follow: Follow): Long

    @Query("DELETE FROM follow WHERE owner_id = :ownerId AND follower_id = :followId")
    suspend fun deleteFollow(ownerId: Long, followId: Long): Int
}