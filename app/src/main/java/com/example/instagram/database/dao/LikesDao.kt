package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.instagram.database.entity.Likes

@Dao
interface LikesDao {
    @Insert
    suspend fun insertNewLike(likes: Likes): Long

    @Query("DELETE FROM likes WHERE profile_id = :profileId AND post_id = :postId")
    suspend fun deleteLikeWithProfileId(profileId: Long, postId: Long): Int

    @Query("SELECT COUNT(post_id) FROM likes WHERE post_id = :postId")
    suspend fun getLikeCountOnPost(postId: Long): Int

    @Query("SELECT COUNT(profile_id) FROM likes WHERE profile_id = :profileId AND post_id = :postId")
    suspend fun isPostLikedByProfile(postId: Long, profileId: Long): Int


}