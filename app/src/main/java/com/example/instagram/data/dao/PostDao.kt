package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.data.entity.Post

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPost(post: Post): Long
    
    @Query("SELECT postId FROM post WHERE profileId = :profileId ORDER BY postTime DESC LIMIT 15 OFFSET :offset")
    suspend fun getPostOfProfile(profileId: Long, offset: Int): MutableList<Long>
    
    @Query("SELECT postId FROM post WHERE profileId = :profileId ORDER BY postTime DESC")
    suspend fun getAllPostOfProfile(profileId: Long): MutableList<Long>
    
    @Query("SELECT COUNT(profileId) FROM post WHERE profileId = :profileId")
    suspend fun getPostCount(profileId: Long): Int
    
    @Query("SELECT username FROM login_credential WHERE profileId = (SELECT profileId FROM post WHERE postId = :postId)")
    suspend fun getUsername(postId: Long): String
    
    @Query("SELECT postTime FROM post WHERE postId = :postId")
    suspend fun getPostTime(postId: Long): Long
    
    @Query("Select profileId from post where postId = :postId")
    suspend fun getProfileId(postId: Long): Long
    
    @Query("Select postId FROM post WHERE profileId IN (SELECT followerId FROM follow WHERE ownerId = :loggedInId) ORDER BY postTime DESC LIMIT :limit OFFSET :offset")
    suspend fun getPostOfFollowers(loggedInId: Long, limit: Int, offset: Int): MutableList<Long>
    
    @Query("select postId from post where profileId = :profileId order by postTime DESC Limit :limit Offset :offset")
    suspend fun getAllPosts(profileId: Long, limit: Int, offset: Int): MutableList<Long>
    
    @Query("DELETE FROM post WHERE post.postId = :postId")
    suspend fun deletePost(postId: Long)
    
    @Query("SELECT placeId FROM post WHERE postId = :postId")
    suspend fun getLocationId(postId: Long): String?
    
    @Query("SELECT postId from post where profileId <> :loggedInId AND profileId NOT IN (SELECT blockerId from blocked_users where blockedId = :loggedInId) AND placeId = :placeId Order by postId desc limit :limit OFFSET :offset")
    suspend fun getPostIdsFromPlaceId(placeId: String, loggedInId: Long, limit: Int, offset: Int): List<Long>
}