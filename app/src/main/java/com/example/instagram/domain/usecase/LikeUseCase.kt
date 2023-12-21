package com.example.instagram.domain.usecase

import android.util.Log
import com.example.instagram.database.entity.Likes
import com.example.instagram.domain.repo_contract.LikeRepo

private const val TAG = "LikePostUseCase_CommTag"

class LikeUseCase(private val likeRepo: LikeRepo) {
    suspend fun likePost(postId: Long, profileId: Long): Long {
        return likeRepo.likePost(Likes(postId, profileId, System.currentTimeMillis()))
    }
    
    suspend fun removeLike(postId: Long, profileId: Long) {
        val deleteLikeCount = likeRepo.deleteLike(profileId, postId)
        Log.d(TAG, "LikeUseCase: delete = $deleteLikeCount")
    }
}