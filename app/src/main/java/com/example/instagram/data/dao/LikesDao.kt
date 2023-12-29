package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.instagram.data.entity.Likes
import com.example.instagram.screen_notification.model.LikeLog

@Dao
interface LikesDao {
    @Insert
    suspend fun insertNewLike(likes: Likes): Long

    @Query("DELETE FROM likes WHERE profileId = :profileId AND postId = :postId")
    suspend fun deleteLike(profileId: Long, postId: Long): Int

    @Query("SELECT COUNT(postId) FROM likes WHERE postId = :postId")
    suspend fun likeCount(postId: Long): Int

    @Query("SELECT COUNT(profileId) FROM likes WHERE profileId = :profileId AND postId = :postId")
    suspend fun isPostLikedByProfile(postId: Long, profileId: Long): Int

    @Query("SELECT likes.profileId, login_credential.username, likes.postId, likes.likeTime FROM likes LEFT JOIN login_credential ON login_credential.profileId = likes.profileId WHERE likes.postId IN (SELECT post.postId FROM post WHERE profileId = :ownerId) AND likes.profileId != :ownerId")
    suspend fun getLikeLog(ownerId: Long): List<LikeLog>
}