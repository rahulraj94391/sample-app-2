package com.example.instagram.domain.repo_contract

import com.example.instagram.data.entity.Likes
import com.example.instagram.screen_notification.model.LikeLog

interface LikeRepo {
    suspend fun likePost(likes: Likes): Long
    suspend fun deleteLike(profileId: Long, postId: Long): Int
    suspend fun getLikeCount(postId: Long): Int
    suspend fun checkLikeByProfile(postId: Long, profileId: Long): Int
    suspend fun getLikeLog(profileId: Long): List<LikeLog>
}