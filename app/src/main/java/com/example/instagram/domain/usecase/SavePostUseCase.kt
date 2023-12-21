package com.example.instagram.domain.usecase

import com.example.instagram.database.entity.SavedPost
import com.example.instagram.domain.repo_contract.SavedPostRepo

class SavePostUseCase(private val savedPostRepo: SavedPostRepo) {
    
    suspend fun savePost(profileId: Long, postId: Long): Long {
        return savedPostRepo.savePost(SavedPost(profileId, postId, System.currentTimeMillis()))
    }
    
    suspend fun removeSavedPost(profileId: Long, postId: Long): Int {
        return savedPostRepo.deleteSavedPost(postId, profileId)
    }
}