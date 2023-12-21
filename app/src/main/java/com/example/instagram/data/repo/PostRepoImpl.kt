package com.example.instagram.data.repo

import com.example.instagram.database.dao.PostDao
import com.example.instagram.database.entity.Post
import com.example.instagram.domain.repo_contract.PostRepo

class PostRepoImpl(private val dao: PostDao) : PostRepo {
    
    override suspend fun insertPost(post: Post): Long {
        return dao.insertPost(post)
    }
    
    override suspend fun getPostOfProfile(profileId: Long, offset: Int): MutableList<Long> {
        return dao.getPostOfProfile(profileId, offset)
    }
    
    override suspend fun getAllPostOfProfile(profileId: Long): MutableList<Long> {
        return dao.getAllPostOfProfile(profileId)
    }
    
    override suspend fun getPostCount(profileId: Long): Int {
        return dao.getPostCount(profileId)
    }
    
    override suspend fun getUsername(postId: Long): String {
        return dao.getUsername(postId)
    }
    
    override suspend fun getPostTime(postId: Long): Long {
        return dao.getPostTime(postId)
    }
    
    override suspend fun getProfileId(postId: Long): Long {
        return dao.getProfileId(postId)
    }
    
    override suspend fun getPostOfFollowers(loggedInId: Long, limit: Int, offset: Int): MutableList<Long> {
        return dao.getPostOfFollowers(loggedInId, limit, offset)
    }
    
    override suspend fun getAllPosts(profileId: Long, limit: Int, offset: Int): MutableList<Long> {
        return dao.getAllPosts(profileId, limit, offset)
    }
    
    override suspend fun deletePost(postId: Long) {
        dao.deletePost(postId)
    }
    
    override suspend fun getLocationId(postId: Long): String? {
        return dao.getLocationId(postId)
    }
    
    override suspend fun getPostIdsFromPlaceId(placeId: String, loggedInId: Long, limit: Int, offset: Int): List<Long> {
        return dao.getPostIdsFromPlaceId(placeId, loggedInId, limit, offset)
    }
}