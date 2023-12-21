package com.example.instagram.data.repo

import com.example.instagram.database.dao.BlockDao
import com.example.instagram.database.entity.BlockedUsers
import com.example.instagram.domain.repo_contract.BlockRepo

class BlockRepoImpl(private val dao: BlockDao) : BlockRepo {
    override suspend fun isBlocked(userA: Long, userB: Long): Int {
        return dao.isBlocked(userA, userB)
    }
    
    override suspend fun unblockUser(myId: Long, userId: Long): Int {
        return dao.unblockUser(myId, userId)
    }
    
    override suspend fun blockUser(blockedUsers: BlockedUsers): Long {
        return dao.blockUser(blockedUsers)
    }
}