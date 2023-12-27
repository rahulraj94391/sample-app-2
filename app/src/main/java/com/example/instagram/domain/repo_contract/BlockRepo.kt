package com.example.instagram.domain.repo_contract

import com.example.instagram.data.entity.BlockedUsers

interface BlockRepo {
    /**
     * return 1 when 'userA' has blocked 'userB'
     */
    suspend fun isBlocked(userA: Long, userB: Long): Int
    suspend fun unblockUser(myId: Long, userId: Long): Int
    suspend fun blockUser(blockedUsers: BlockedUsers): Long
}