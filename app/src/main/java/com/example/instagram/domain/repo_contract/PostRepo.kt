package com.example.instagram.domain.repo_contract

import com.example.instagram.database.entity.Post

interface PostRepo {
    suspend fun insertPost(post: Post): Long
    suspend fun getPostOfProfile(profileId: Long, offset: Int): MutableList<Long>
    suspend fun getAllPostOfProfile(profileId: Long): MutableList<Long>
    suspend fun getPostCount(profileId: Long): Int
    suspend fun getUsername(postId: Long): String
    suspend fun getPostTime(postId: Long): Long
    suspend fun getProfileId(postId: Long): Long
    suspend fun getPostOfFollowers(loggedInId: Long, limit: Int, offset: Int): MutableList<Long>
    suspend fun getAllPosts(profileId: Long, limit: Int, offset: Int): MutableList<Long>
    suspend fun deletePost(postId: Long)
    suspend fun getLocationId(postId: Long): String?
    suspend fun getPostIdsFromPlaceId(placeId: String, loggedInId: Long, limit: Int, offset: Int): List<Long>
    
}