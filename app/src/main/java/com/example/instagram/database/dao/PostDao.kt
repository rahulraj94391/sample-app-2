package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.database.entity.Post

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPost(post: Post): Long
    
    @Query("SELECT post_id FROM post WHERE profile_id = :profileId ORDER BY post_time DESC LIMIT 15 OFFSET :offset")
    suspend fun getAllPostOfProfile(profileId: Long, offset: Int): MutableList<Long>
    
    @Query("SELECT COUNT(profile_id) FROM post WHERE profile_id = :profileId")
    suspend fun getPostCount(profileId: Long): Int
    
    @Query("SELECT username FROM login_credential WHERE profile_id = (SELECT profile_id FROM post WHERE post_id = :postId)")
    suspend fun getUsername(postId: Long): String
    
    @Query("SELECT post_time FROM post WHERE post_id = :postId")
    suspend fun getPostTime(postId: Long): Long
    
    @Query("Select profile_id from post where post_id = :postId")
    suspend fun getProfileId(postId: Long): Long
    
    @Query("Select post_id FROM post WHERE profile_id IN (SELECT follower_id FROM follow WHERE owner_id = :loggedInId) ORDER BY post_time DESC LIMIT :limit OFFSET :offset")
    suspend fun getPostOfFollowers(loggedInId: Long, limit: Int, offset: Int): MutableList<Long>
    
    @Query("DELETE FROM post WHERE post.post_id = :postId")
    suspend fun deletePost(postId: Long)
}