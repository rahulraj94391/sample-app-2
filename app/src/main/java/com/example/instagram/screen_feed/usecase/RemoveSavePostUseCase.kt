package com.example.instagram.screen_feed.usecase

import com.example.instagram.domain.repo_contract.SavedPostRepo

class RemoveSavePostUseCase(private val savedPostRepo: SavedPostRepo) {
    
    suspend operator fun invoke(profileId: Long, postId: Long): Int {
        return savedPostRepo.deleteSavedPost(postId, profileId)
        
    }
    
}