package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.instagram.data.entity.SavedPost

@Dao
interface SavedPostDao {
    @Query("SELECT COUNT(profileId) FROM saved_post WHERE profileId = :profileId AND postId = :postId")
    suspend fun isPostSavedByProfile(profileId: Long, postId: Long): Int

    @Insert
    suspend fun savePost(savedPost: SavedPost): Long

    @Query("DELETE FROM saved_post WHERE profileId = :profileId AND postId = :postId")
    suspend fun deleteSavedPost(postId: Long, profileId: Long): Int

    @Query("SELECT postId FROM saved_post WHERE profileId = :profileId")
    suspend fun getAllSavedPosts(profileId: Long): MutableList<Long>

}