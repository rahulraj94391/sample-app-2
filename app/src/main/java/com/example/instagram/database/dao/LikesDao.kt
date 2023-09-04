package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.instagram.database.entity.Likes
import com.example.instagram.database.model.LikeLog

@Dao
interface LikesDao {
    @Insert
    suspend fun insertNewLike(likes: Likes): Long

    @Query("DELETE FROM likes WHERE profile_id = :profileId AND post_id = :postId")
    suspend fun deleteLike(profileId: Long, postId: Long): Int

    @Query("SELECT COUNT(post_id) FROM likes WHERE post_id = :postId")
    suspend fun likeCount(postId: Long): Int

    @Query("SELECT COUNT(profile_id) FROM likes WHERE profile_id = :profileId AND post_id = :postId")
    suspend fun isPostLikedByProfile(postId: Long, profileId: Long): Int

    @Query("SELECT likes.profile_id, login_credential.username, likes.post_id, likes.like_time FROM likes LEFT JOIN login_credential ON login_credential.profile_id = likes.profile_id WHERE likes.post_id IN (SELECT post.post_id FROM post WHERE profile_id = :ownerId) AND likes.profile_id != :ownerId")
    suspend fun getLikeLog(ownerId: Long): List<LikeLog>

}