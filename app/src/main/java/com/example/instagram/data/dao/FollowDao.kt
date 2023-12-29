package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.data.entity.Follow
import com.example.instagram.screen_connections.model.Connection
import com.example.instagram.screen_notification.model.FollowLog

@Dao
interface FollowDao {
    @Query("SELECT COUNT(followerId) FROM follow WHERE followerId = :profileId")
    suspend fun getFollowerCount(profileId: Long): Int
    
    @Query("SELECT COUNT(ownerId) FROM follow WHERE ownerId = :profileId")
    suspend fun getFollowingCount(profileId: Long): Int
    
    @Query("SELECT COUNT(followerId) FROM follow WHERE ownerId = :ownId AND followerId = :userId")
    suspend fun isUserFollowingUser(ownId: Long, userId: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewFollow(follow: Follow): Long
    
    @Query("DELETE FROM follow WHERE ownerId = :ownerId AND followerId = :followId")
    suspend fun deleteFollow(ownerId: Long, followId: Long): Int
    
    @Query("SELECT profile.profileId, profile.firstName, profile.lastName, login_credential.username FROM profile LEFT JOIN login_credential ON profile.profileId = login_credential.profileId WHERE profile.profileId IN (SELECT ownerId FROM follow WHERE followerId = :profileId)")
    suspend fun getFollowerList(profileId: Long): MutableList<Connection>
    
    @Query("SELECT profile.profileId, profile.firstName, profile.lastName, login_credential.username FROM profile LEFT JOIN login_credential ON profile.profileId = login_credential.profileId WHERE profile.profileId IN (SELECT followerId FROM follow WHERE ownerId = :profileId)")
    suspend fun getFollowingList(profileId: Long): MutableList<Connection>
    
    @Query("select follow.ownerId, follow.time, login_credential.username from follow left join login_credential on follow.ownerId = login_credential.profileId where follow.followerId = :ownerId ORDER BY follow.time DESC")
    suspend fun getFollowLog(ownerId: Long): List<FollowLog>
}