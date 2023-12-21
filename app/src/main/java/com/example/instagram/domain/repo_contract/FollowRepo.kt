package com.example.instagram.domain.repo_contract

import com.example.instagram.database.entity.Follow
import com.example.instagram.database.model.FollowList
import com.example.instagram.database.model.FollowLog

interface FollowRepo {
    suspend fun getFollowerCount(profileId: Long): Int
    suspend fun getFollowingCount(profileId: Long): Int
    suspend fun isUserFollowingUser(ownId: Long, userId: Long): Int
    suspend fun insertNewFollow(follow: Follow): Long
    suspend fun deleteFollow(ownerId: Long, followId: Long): Int
    suspend fun getFollowerList(profileId: Long): MutableList<FollowList>
    suspend fun getFollowingList(profileId: Long): MutableList<FollowList>
    suspend fun getFollowLog(ownerId: Long): List<FollowLog>
}