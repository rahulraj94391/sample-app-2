package com.example.instagram.screen_feed.usecase

import com.example.instagram.data.entity.Likes
import com.example.instagram.domain.repo_contract.LikeRepo

private const val TAG = "LikePostUseCase_CommTag"

class LikeUseCase(private val likeRepo: LikeRepo) {
    
    suspend operator fun invoke(postId: Long, profileId: Long): Long {
        return likeRepo.likePost(Likes(postId, profileId, System.currentTimeMillis()))
    }
}