package com.example.instagram.domain.repo_contract

import com.example.instagram.database.entity.SavedPost

interface SavedPostRepo {
    suspend fun isPostSavedByProfile(profileId: Long, postId: Long): Int
    suspend fun savePost(savedPost: SavedPost): Long
    suspend fun deleteSavedPost(postId: Long, profileId: Long): Int
    suspend fun getAllSavedPosts(profileId: Long): MutableList<Long>
}