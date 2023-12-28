package com.example.instagram.screen_feed.usecase

import com.example.instagram.data.entity.SavedPost
import com.example.instagram.domain.repo_contract.SavedPostRepo

class SavePostUseCase(private val savedPostRepo: SavedPostRepo) {
    
    suspend operator fun invoke(profileId: Long, postId: Long): Long {
        return savedPostRepo.savePost(SavedPost(profileId, postId, System.currentTimeMillis()))
    }
}