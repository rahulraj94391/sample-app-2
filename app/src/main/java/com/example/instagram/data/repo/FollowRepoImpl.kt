package com.example.instagram.data.repo

import com.example.instagram.data.dao.FollowDao
import com.example.instagram.data.entity.Follow
import com.example.instagram.screen_connections.model.Connection
import com.example.instagram.screen_notification.model.FollowLog
import com.example.instagram.domain.repo_contract.FollowRepo

class FollowRepoImpl(private val dao: FollowDao) : FollowRepo {
    override suspend fun getFollowerCount(profileId: Long): Int {
        return dao.getFollowerCount(profileId)
    }
    
    override suspend fun getFollowingCount(profileId: Long): Int {
        return dao.getFollowingCount(profileId)
    }
    
    override suspend fun isUserFollowingUser(ownId: Long, userId: Long): Int {
        return dao.isUserFollowingUser(ownId, userId)
    }
    
    override suspend fun insertNewFollow(follow: Follow): Long {
        return dao.insertNewFollow(follow)
    }
    
    override suspend fun deleteFollow(ownerId: Long, followId: Long): Int {
        return dao.deleteFollow(ownerId, followId)
    }
    
    override suspend fun getFollowerList(profileId: Long): MutableList<Connection> {
        return dao.getFollowerList(profileId)
    }
    
    override suspend fun getFollowingList(profileId: Long): MutableList<Connection> {
        return dao.getFollowingList(profileId)
    }
    
    override suspend fun getFollowLog(ownerId: Long): List<FollowLog> {
        return dao.getFollowLog(ownerId)
    }
}