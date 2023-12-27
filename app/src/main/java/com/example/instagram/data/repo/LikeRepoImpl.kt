package com.example.instagram.data.repo

import com.example.instagram.data.dao.LikesDao
import com.example.instagram.data.entity.Likes
import com.example.instagram.screen_notification.model.LikeLog
import com.example.instagram.domain.repo_contract.LikeRepo

private const val TAG = "LikeRepoImpl_CommTag"

class LikeRepoImpl(private val dao: LikesDao) : LikeRepo {
    override suspend fun likePost(likes: Likes): Long {
        return dao.insertNewLike(likes)
    }
    
    override suspend fun deleteLike(profileId: Long, postId: Long): Int {
        return dao.deleteLike(profileId, postId)
    }
    
    override suspend fun getLikeCount(postId: Long): Int {
        return dao.likeCount(postId)
    }
    
    override suspend fun checkLikeByProfile(postId: Long, profileId: Long): Int {
        return dao.isPostLikedByProfile(postId, profileId)
    }
    
    override suspend fun getLikeLog(profileId: Long): List<LikeLog> {
        return dao.getLikeLog(profileId)
    }
    
}