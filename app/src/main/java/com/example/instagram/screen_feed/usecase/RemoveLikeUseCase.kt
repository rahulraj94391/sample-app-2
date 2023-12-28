package com.example.instagram.screen_feed.usecase

import android.util.Log
import com.example.instagram.domain.repo_contract.LikeRepo

private const val TAG = "RemoveLikeUseCase_CommTag"

class RemoveLikeUseCase(private val likeRepo: LikeRepo) {
    suspend operator fun invoke(postId: Long, profileId: Long) {
        val deleteLikeCount = likeRepo.deleteLike(profileId, postId)
        Log.d(TAG, "LikeUseCase: delete = $deleteLikeCount")
    }
}