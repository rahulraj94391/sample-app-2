package com.example.instagram.data.repo

import com.example.instagram.data.dao.LastOnlineDao
import com.example.instagram.data.entity.LastOnline
import com.example.instagram.domain.repo_contract.LastOnlineRepo

class LastOnlineRepoImpl(private val dao: LastOnlineDao) : LastOnlineRepo {
    override suspend fun getUserLastOnlineStatus(userId: Long, myId: Long): LastOnline? {
        return dao.getUserLastOnlineStatus(userId, myId)
    }
    
    override suspend fun getMyLastOnlineTime(myId: Long, userId: Long): LastOnline? {
        return dao.getMyLastOnlineTime(myId, userId)
    }
    
    override suspend fun updateMyLastOnlineStatus(lastOnline: LastOnline): Int {
        return dao.updateMyLastOnlineStatus(lastOnline)
    }
    
    override suspend fun insertMyLastOnlineStatus(lastStatus: LastOnline): Long {
        return dao.insertMyLastOnlineStatus(lastStatus)
    }
}