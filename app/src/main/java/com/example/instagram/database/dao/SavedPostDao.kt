package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.instagram.database.entity.SavedPost

@Dao
interface SavedPostDao {
    @Query("SELECT COUNT(profile_id) FROM saved_post WHERE profile_id = :profileId AND post_id = :postId")
    suspend fun isPostSavedByProfile(profileId: Long, postId: Long): Int

    @Insert
    suspend fun savePost(savedPost: SavedPost): Long

    @Query("DELETE FROM saved_post WHERE profile_id = :profileId AND post_id = :postId")
    suspend fun deleteSavedPost(postId: Long, profileId: Long): Int


}