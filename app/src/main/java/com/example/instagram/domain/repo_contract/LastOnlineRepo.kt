package com.example.instagram.domain.repo_contract

import com.example.instagram.database.entity.LastOnline

interface LastOnlineRepo {
    suspend fun getUserLastOnlineStatus(userId: Long, myId: Long): LastOnline?
    suspend fun getMyLastOnlineTime(myId: Long, userId: Long): LastOnline?
    suspend fun updateMyLastOnlineStatus(lastOnline: LastOnline): Int
    suspend fun insertMyLastOnlineStatus(lastStatus: LastOnline): Long
}