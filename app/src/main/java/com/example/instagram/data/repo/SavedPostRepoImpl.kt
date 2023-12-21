package com.example.instagram.data.repo

import com.example.instagram.database.dao.SavedPostDao
import com.example.instagram.database.entity.SavedPost
import com.example.instagram.domain.repo_contract.SavedPostRepo

class SavedPostRepoImpl(private val dao: SavedPostDao) : SavedPostRepo {
    override suspend fun isPostSavedByProfile(profileId: Long, postId: Long): Int {
        return dao.isPostSavedByProfile(profileId, postId)
    }
    
    override suspend fun savePost(savedPost: SavedPost): Long {
        return dao.savePost(savedPost)
    }
    
    override suspend fun deleteSavedPost(postId: Long, profileId: Long): Int {
        return dao.deleteSavedPost(postId, profileId)
    }
    
    override suspend fun getAllSavedPosts(profileId: Long): MutableList<Long> {
        return dao.getAllSavedPosts(profileId)
    }
}