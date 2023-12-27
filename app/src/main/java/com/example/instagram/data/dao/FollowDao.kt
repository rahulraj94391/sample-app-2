package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.data.entity.Follow
import com.example.instagram.screen_followerAndFollowingView.model.Connection
import com.example.instagram.screen_notification.model.FollowLog

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
    
    @Query("SELECT profile.profile_id, profile.first_name, profile.last_name, login_credential.username FROM profile LEFT JOIN login_credential ON profile.profile_id = login_credential.profile_id WHERE profile.profile_id IN (SELECT owner_id FROM follow WHERE follower_id = :profileId)")
    suspend fun getFollowerList(profileId: Long): MutableList<Connection>
    
    @Query("SELECT profile.profile_id, profile.first_name, profile.last_name, login_credential.username FROM profile LEFT JOIN login_credential ON profile.profile_id = login_credential.profile_id WHERE profile.profile_id IN (SELECT follower_id FROM follow WHERE owner_id = :profileId)")
    suspend fun getFollowingList(profileId: Long): MutableList<Connection>
    
    @Query("select follow.owner_id, follow.time, login_credential.username from follow left join login_credential on follow.owner_id = login_credential.profile_id where follow.follower_id = :ownerId ORDER BY follow.time DESC")
    suspend fun getFollowLog(ownerId: Long): List<FollowLog>
}